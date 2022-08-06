package org.apache.flink.runtime.dispatcher;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.blob.BlobSharedClient;
import org.apache.flink.runtime.heartbeat.HeartbeatServices;
import org.apache.flink.runtime.highavailability.HighAvailabilityServices;
import org.apache.flink.runtime.resourcemanager.ResourceManagerGateway;
import org.apache.flink.runtime.rpc.FatalErrorHandler;
import org.apache.flink.runtime.webmonitor.retriever.GatewayRetriever;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.concurrent.Executor;

public class PartialJobMasterDispatcherServices {

    @Nonnull
    private final Configuration configuration;

    @Nonnull private final HighAvailabilityServices highAvailabilityServices;

    @Nonnull private final GatewayRetriever<ResourceManagerGateway> resourceManagerGatewayRetriever;

    @Nonnull private final BlobSharedClient blobSharedClient;

    @Nonnull private final HeartbeatServices heartbeatServices;

    @Nonnull private final JobManagerMetricGroupFactory jobManagerMetricGroupFactory;

    @Nonnull private final ExecutionGraphInfoStore executionGraphInfoStore;

    @Nonnull private final FatalErrorHandler fatalErrorHandler;

    @Nullable
    private final String metricQueryServiceAddress;

    @Nonnull private final DispatcherOperationCaches operationCaches;

    @Nonnull private final Executor ioExecutor;

    public PartialJobMasterDispatcherServices(
            @Nonnull Configuration configuration,
            @Nonnull HighAvailabilityServices highAvailabilityServices,
            @Nonnull GatewayRetriever<ResourceManagerGateway> resourceManagerGatewayRetriever,
            @Nonnull BlobSharedClient blobSharedClient,
            @Nonnull HeartbeatServices heartbeatServices,
            @Nonnull JobManagerMetricGroupFactory jobManagerMetricGroupFactory,
            @Nonnull ExecutionGraphInfoStore executionGraphInfoStore,
            @Nonnull FatalErrorHandler fatalErrorHandler,
            @Nullable String metricQueryServiceAddress,
            @Nonnull Executor ioExecutor,
            @Nonnull DispatcherOperationCaches operationCaches) {
        this.configuration = configuration;
        this.highAvailabilityServices = highAvailabilityServices;
        this.resourceManagerGatewayRetriever = resourceManagerGatewayRetriever;
        this.blobSharedClient = blobSharedClient;
        this.heartbeatServices = heartbeatServices;
        this.jobManagerMetricGroupFactory = jobManagerMetricGroupFactory;
        this.executionGraphInfoStore = executionGraphInfoStore;
        this.fatalErrorHandler = fatalErrorHandler;
        this.metricQueryServiceAddress = metricQueryServiceAddress;
        this.ioExecutor = ioExecutor;
        this.operationCaches = operationCaches;
    }

}
