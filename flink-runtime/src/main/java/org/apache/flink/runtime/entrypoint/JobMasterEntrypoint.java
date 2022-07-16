/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.flink.runtime.entrypoint;

import org.apache.flink.api.common.time.Time;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.CoreOptions;
import org.apache.flink.configuration.JobManagerOptions;
import org.apache.flink.configuration.SchedulerExecutionMode;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.core.plugin.PluginManager;
import org.apache.flink.core.security.FlinkSecurityManager;
import org.apache.flink.runtime.blob.BlobSharedClient;
import org.apache.flink.runtime.dispatcher.JobManagerRunnerFactory;
import org.apache.flink.runtime.dispatcher.JobMasterServiceLeadershipRunnerFactory;
import org.apache.flink.runtime.execution.librarycache.BlobLibraryCacheManager;
import org.apache.flink.runtime.execution.librarycache.LibraryCacheManager;
import org.apache.flink.runtime.heartbeat.HeartbeatServices;
import org.apache.flink.runtime.highavailability.HighAvailabilityServices;
import org.apache.flink.runtime.highavailability.HighAvailabilityServicesUtils;
import org.apache.flink.runtime.jobgraph.JobGraph;
import org.apache.flink.runtime.jobmaster.DefaultSlotPoolServiceSchedulerFactory;
import org.apache.flink.runtime.jobmaster.JobManagerRunner;

import org.apache.flink.runtime.jobmaster.JobManagerSharedServices;

import org.apache.flink.runtime.jobmaster.SlotPoolServiceSchedulerFactory;
import org.apache.flink.runtime.jobmaster.factories.JobManagerJobMetricGroupFactory;
import org.apache.flink.runtime.rpc.AddressResolution;
import org.apache.flink.runtime.rpc.FatalErrorHandler;
import org.apache.flink.runtime.rpc.RpcService;
import org.apache.flink.runtime.rpc.RpcSystem;
import org.apache.flink.runtime.rpc.RpcSystemUtils;
import org.apache.flink.runtime.rpc.RpcUtils;
import org.apache.flink.runtime.security.SecurityConfiguration;
import org.apache.flink.runtime.security.SecurityUtils;
import org.apache.flink.runtime.security.contexts.SecurityContext;
import org.apache.flink.runtime.shuffle.ShuffleMaster;
import org.apache.flink.runtime.shuffle.ShuffleMasterContext;
import org.apache.flink.runtime.shuffle.ShuffleMasterContextImpl;
import org.apache.flink.runtime.shuffle.ShuffleServiceLoader;
import org.apache.flink.runtime.util.EnvironmentInformation;
import org.apache.flink.runtime.util.Hardware;
import org.apache.flink.runtime.util.JvmShutdownSafeguard;
import org.apache.flink.runtime.util.SignalHandler;
import org.apache.flink.util.FlinkUserCodeClassLoaders;

import org.apache.flink.util.Preconditions;
import org.apache.flink.util.concurrent.ExecutorThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.GuardedBy;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/** Entry point for the single jobMaster. */
public class JobMasterEntrypoint implements FatalErrorHandler {

    protected static final Logger LOG = LoggerFactory.getLogger(JobMasterEntrypoint.class);

    protected static final int STARTUP_FAILURE_RETURN_CODE = 1;
    protected static final int RUNTIME_FAILURE_RETURN_CODE = 2;

    /** The lock to guard startup / shutdown / manipulation methods. */
    private final Object lock = new Object();

    private final AtomicBoolean isShutDown = new AtomicBoolean(false);

    private static final Time INITIALIZATION_SHUTDOWN_TIMEOUT = Time.seconds(30L);

    @GuardedBy("lock")
    private ScheduledExecutorService futureExecutor;

    @GuardedBy("lock")
    private RpcService rpcService;

    private JobGraph jobGraph;
    private HighAvailabilityServices haServices;

    private SlotPoolServiceSchedulerFactory slotPoolServiceSchedulerFactory;

    private BlobSharedClient blobSharedClient;
    private HeartbeatServices heartbeatServices;
    private JobManagerJobMetricGroupFactory jobManagerJobMetricGroupFactory;

    private JobManagerSharedServices jobManagerSharedServices;

    private ClassLoader userCodeClassloader;

    private Configuration configuration;

    private RpcSystem rpcSystem;

    private JobManagerRunner jobManagerRunner;

    public static void main(String[] args) {
        EnvironmentInformation.logEnvironmentInfo(
                LOG, JobMasterEntrypoint.class.getSimpleName(), args);
        SignalHandler.register(LOG);
        JvmShutdownSafeguard.installAsShutdownHook(LOG);
    }

    protected void initializeServices(Configuration configuration) throws Exception {

        LOG.info("Initializing jobMaster services form jobId.");
        // conf
        this.configuration = configuration;

        this.blobSharedClient =
                new BlobSharedClient(
                        new InetSocketAddress(
                                configuration.getString(JobManagerOptions.BIND_HOST),
                                configuration.getInteger(JobManagerOptions.RPC_BIND_PORT)),
                        configuration);

        // JobManagerSharedServices

        this.jobManagerSharedServices =
                this.createJobManagerSharedServices(configuration, blobSharedClient);

        // Heartbeat

        this.heartbeatServices = createHeartbeatServices(configuration);

        // RPC

        rpcSystem = RpcSystem.load(configuration);
        this.rpcService =
                RpcUtils.createRemoteRpcService(
                        rpcSystem,
                        configuration,
                        configuration.getString(JobManagerOptions.ADDRESS),
                        String.valueOf(configuration.getInteger(JobManagerOptions.PORT)),
                        configuration.getString(JobManagerOptions.BIND_HOST),
                        configuration.getOptional(JobManagerOptions.RPC_BIND_PORT));

        // ha
        haServices = createHaServices(configuration, jobManagerSharedServices.getIoExecutor(), rpcSystem);

        // slot pool
        slotPoolServiceSchedulerFactory =
                DefaultSlotPoolServiceSchedulerFactory.fromConfiguration(
                        configuration, jobGraph.getJobType());

        if (configuration.get(JobManagerOptions.SCHEDULER_MODE)
                == SchedulerExecutionMode.REACTIVE) {
            Preconditions.checkState(
                    slotPoolServiceSchedulerFactory.getSchedulerType()
                            == JobManagerOptions.SchedulerType.Adaptive,
                    "Adaptive Scheduler is required for reactive mode");
        }
        this.jobManagerRunner = createJobManagerRunner();
    }

    public CompletableFuture<Void> stopJobMasterService() throws ClusterEntrypointException {
        LOG.info("Stopping {}.", getClass().getSimpleName());
        return jobManagerRunner.closeAsync();
    }

    private void configureFileSystems(Configuration configuration, PluginManager pluginManager) {
        LOG.info("Install default filesystem.");
        FileSystem.initialize(configuration, pluginManager);
    }

    private SecurityContext installSecurityContext(Configuration configuration) throws Exception {
        LOG.info("Install security context.");

        SecurityUtils.install(new SecurityConfiguration(configuration));
        return SecurityUtils.getInstalledContext();
    }

    protected HeartbeatServices createHeartbeatServices(Configuration configuration) {
        return HeartbeatServices.fromConfiguration(configuration);
    }

    protected HighAvailabilityServices createHaServices(
            Configuration configuration, Executor executor, RpcSystemUtils rpcSystemUtils)
            throws Exception {
        return HighAvailabilityServicesUtils.createHighAvailabilityServices(
                configuration,
                executor,
                AddressResolution.NO_ADDRESS_RESOLUTION,
                rpcSystemUtils,
                this);
    }


    protected JobManagerRunner createJobManagerRunner() throws Exception {
        JobManagerRunnerFactory jobManagerRunnerFactory =
                JobMasterServiceLeadershipRunnerFactory.INSTANCE;
        return jobManagerRunnerFactory.createJobManagerRunner(
                jobGraph,
                configuration,
                rpcService,
                haServices,
                heartbeatServices,
                jobManagerSharedServices,
                jobManagerJobMetricGroupFactory,
                this,
                System.currentTimeMillis());
    }


    /**
     * create jobManager Shared Services
     *
     * @param configuration
     * @param client
     * @return
     * @throws Exception
     */
    protected JobManagerSharedServices createJobManagerSharedServices(
            Configuration configuration, BlobSharedClient client) throws Exception {

        final String classLoaderResolveOrder =
                configuration.getString(CoreOptions.CLASSLOADER_RESOLVE_ORDER);

        final String[] alwaysParentFirstLoaderPatterns =
                CoreOptions.getParentFirstLoaderPatterns(configuration);

        final boolean failOnJvmMetaspaceOomError =
                configuration.getBoolean(CoreOptions.FAIL_ON_USER_CLASS_LOADING_METASPACE_OOM);
        final boolean checkClassLoaderLeak =
                configuration.getBoolean(CoreOptions.CHECK_LEAKED_CLASSLOADER);
        final BlobLibraryCacheManager libraryCacheManager =
                new BlobLibraryCacheManager(
                        client,
                        BlobLibraryCacheManager.defaultClassLoaderFactory(
                                FlinkUserCodeClassLoaders.ResolveOrder.fromString(
                                        classLoaderResolveOrder),
                                alwaysParentFirstLoaderPatterns,
                                failOnJvmMetaspaceOomError ? this : null,
                                checkClassLoaderLeak));

        final int numberCPUCores = Hardware.getNumberCPUCores();
        final int jobManagerFuturePoolSize =
                configuration.getInteger(
                        JobManagerOptions.JOB_MANAGER_FUTURE_POOL_SIZE, numberCPUCores);
        final ScheduledExecutorService futureExecutor =
                Executors.newScheduledThreadPool(
                        jobManagerFuturePoolSize, new ExecutorThreadFactory("jobmanager-future"));

        final int jobManagerIoPoolSize =
                configuration.getInteger(
                        JobManagerOptions.JOB_MANAGER_IO_POOL_SIZE, numberCPUCores);
        final ExecutorService ioExecutor =
                Executors.newFixedThreadPool(
                        jobManagerIoPoolSize, new ExecutorThreadFactory("jobmanager-io"));

        final ShuffleMasterContext shuffleMasterContext =
                new ShuffleMasterContextImpl(configuration, this);
        final ShuffleMaster<?> shuffleMaster =
                ShuffleServiceLoader.loadShuffleServiceFactory(configuration)
                        .createShuffleMaster(shuffleMasterContext);
        shuffleMaster.start();

        return new JobManagerSharedServices(
                futureExecutor, ioExecutor, libraryCacheManager, shuffleMaster, client);
    }

    private void runJobMaster(Configuration configuration) throws Exception {

    }

    private void stopJobMaster(Configuration configuration) throws Exception {

    }

    @Override
    public void onFatalError(Throwable exception) {
        ClusterEntryPointExceptionUtils.tryEnrichClusterEntryPointError(exception);
        LOG.error("Fatal error occurred in the cluster entrypoint.", exception);
        FlinkSecurityManager.forceProcessExit(RUNTIME_FAILURE_RETURN_CODE);
    }
}
