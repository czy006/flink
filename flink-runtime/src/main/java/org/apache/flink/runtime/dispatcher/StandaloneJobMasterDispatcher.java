package org.apache.flink.runtime.dispatcher;

import org.apache.flink.runtime.jobgraph.JobGraph;
import org.apache.flink.runtime.jobmaster.JobResult;
import org.apache.flink.runtime.rpc.RpcService;

import java.util.Collection;

public class StandaloneJobMasterDispatcher extends JobMasterDispatcher {
    public StandaloneJobMasterDispatcher(
            RpcService rpcService, DispatcherId fencingToken,
            Collection<JobGraph> recoveredJobs,
            Collection<JobResult> recoveredDirtyJobResults,
            DispatcherBootstrapFactory dispatcherBootstrapFactory,
            DispatcherServices dispatcherServices) throws Exception {
        super(
                rpcService,
                fencingToken,
                recoveredJobs,
                recoveredDirtyJobResults,
                dispatcherBootstrapFactory,
                dispatcherServices);
    }
}
