package org.apache.flink.runtime.entrypoint.component;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.clusterframework.types.ResourceID;
import org.apache.flink.runtime.dispatcher.ExecutionGraphInfoStore;
import org.apache.flink.runtime.heartbeat.HeartbeatServices;
import org.apache.flink.runtime.highavailability.HighAvailabilityServices;
import org.apache.flink.runtime.metrics.MetricRegistry;
import org.apache.flink.runtime.rpc.FatalErrorHandler;
import org.apache.flink.runtime.rpc.RpcService;
import org.apache.flink.runtime.security.token.DelegationTokenManager;
import org.apache.flink.runtime.webmonitor.retriever.MetricQueryServiceRetriever;

import java.util.concurrent.Executor;

public interface JobMasterDispatcherComponentFactory {

    JobMasterDispatcherComponent create(
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
            FatalErrorHandler fatalErrorHandler)
            throws Exception;
}
