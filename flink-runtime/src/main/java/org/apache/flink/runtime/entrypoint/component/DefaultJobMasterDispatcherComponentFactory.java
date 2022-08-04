package org.apache.flink.runtime.entrypoint.component;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.clusterframework.types.ResourceID;
import org.apache.flink.runtime.dispatcher.ExecutionGraphInfoStore;
import org.apache.flink.runtime.dispatcher.runner.DispatcherRunner;
import org.apache.flink.runtime.dispatcher.runner.DispatcherRunnerFactory;
import org.apache.flink.runtime.heartbeat.HeartbeatServices;
import org.apache.flink.runtime.highavailability.HighAvailabilityServices;
import org.apache.flink.runtime.jobmanager.HaServicesJobPersistenceComponentFactory;
import org.apache.flink.runtime.metrics.MetricRegistry;
import org.apache.flink.runtime.rpc.FatalErrorHandler;
import org.apache.flink.runtime.rpc.RpcService;
import org.apache.flink.runtime.security.token.DelegationTokenManager;
import org.apache.flink.runtime.webmonitor.retriever.MetricQueryServiceRetriever;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

import java.util.concurrent.Executor;

public class DefaultJobMasterDispatcherComponentFactory implements JobMasterDispatcherComponentFactory{

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Nonnull
    private final DispatcherRunnerFactory dispatcherRunnerFactory;

    public DefaultJobMasterDispatcherComponentFactory(@Nonnull DispatcherRunnerFactory dispatcherRunnerFactory){
        this.dispatcherRunnerFactory = dispatcherRunnerFactory;
    }

    @Override
    public JobMasterDispatcherComponent create(
            Configuration configuration,
            ResourceID resourceId,
            Executor ioExecutor,
            RpcService rpcService,
            HighAvailabilityServices highAvailabilityServices,
            HeartbeatServices heartbeatServices,
            DelegationTokenManager delegationTokenManager,
            MetricRegistry metricRegistry,
            ExecutionGraphInfoStore executionGraphInfoStore,
            MetricQueryServiceRetriever metricQueryServiceRetriever,
            FatalErrorHandler fatalErrorHandler) throws Exception {

        DispatcherRunner dispatcherRunner = null;
        try {
            log.debug("Starting Dispatcher.");
            dispatcherRunner =
                    dispatcherRunnerFactory.createDispatcherRunner(
                            highAvailabilityServices.getDispatcherLeaderElectionService(),
                            fatalErrorHandler,
                            new HaServicesJobPersistenceComponentFactory(highAvailabilityServices),
                            ioExecutor,
                            rpcService,
                            partialDispatcherServices);
            return new JobMasterDispatcherComponent(dispatcherRunner,fatalErrorHandler，);
        }catch (Exception exception){

        }
        return null;
    }

}
