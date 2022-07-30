package org.apache.flink.runtime.dispatcher.runner;

import org.apache.flink.api.common.JobID;
import org.apache.flink.runtime.clusterframework.ApplicationStatus;
import org.apache.flink.runtime.dispatcher.DispatcherGateway;
import org.apache.flink.runtime.dispatcher.JobMasterDispatcher;

import java.util.concurrent.CompletableFuture;

public class JobMasterDispatcherGatewayService implements AbstractDispatcherLeaderProcess.DispatcherGatewayService {

    private final JobMasterDispatcher jobMasterDispatcher;
    private final DispatcherGateway dispatcherGateway;

    private JobMasterDispatcherGatewayService(JobMasterDispatcher jobMasterDispatcher) {
        this.jobMasterDispatcher = jobMasterDispatcher;
        this.dispatcherGateway = jobMasterDispatcher.getSelfGateway(DispatcherGateway.class);
    }

    @Override
    public DispatcherGateway getGateway() {
        return dispatcherGateway;
    }

    @Override
    public CompletableFuture<Void> onRemovedJobGraph(JobID jobId) {
        return jobMasterDispatcher.onRemovedJobGraph(jobId);
    }

    @Override
    public CompletableFuture<ApplicationStatus> getShutDownFuture() {
        return jobMasterDispatcher.getShutDownFuture();
    }

    @Override
    public CompletableFuture<Void> getTerminationFuture() {
        return jobMasterDispatcher.getTerminationFuture();
    }

    @Override
    public CompletableFuture<Void> closeAsync() {
        return jobMasterDispatcher.closeAsync();
    }

    public static JobMasterDispatcherGatewayService from(JobMasterDispatcher jobMasterDispatcher) {
        return new JobMasterDispatcherGatewayService(jobMasterDispatcher);
    }
}
