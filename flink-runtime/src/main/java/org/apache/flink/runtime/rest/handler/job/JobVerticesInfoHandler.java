package org.apache.flink.runtime.rest.handler.job;

import org.apache.flink.api.common.JobID;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.runtime.executiongraph.AccessExecutionJobVertex;
import org.apache.flink.runtime.executiongraph.AccessExecutionVertex;
import org.apache.flink.runtime.executiongraph.ArchivedExecutionGraph;
import org.apache.flink.runtime.jobgraph.JobVertexID;
import org.apache.flink.runtime.jobgraph.OperatorDescriptor;
import org.apache.flink.runtime.jobgraph.OperatorDescriptorUtils;
import org.apache.flink.runtime.jobgraph.OperatorEdgeDescriptor;
import org.apache.flink.runtime.rest.handler.HandlerRequest;
import org.apache.flink.runtime.rest.handler.RestHandlerException;
import org.apache.flink.runtime.rest.handler.legacy.ExecutionGraphCache;
import org.apache.flink.runtime.rest.handler.legacy.metrics.MetricFetcher;
import org.apache.flink.runtime.rest.handler.legacy.metrics.MetricStore;
import org.apache.flink.runtime.rest.handler.util.MutableIOMetrics;
import org.apache.flink.runtime.rest.messages.EmptyRequestBody;
import org.apache.flink.runtime.rest.messages.JobMessageParameters;
import org.apache.flink.runtime.rest.messages.MessageHeaders;
import org.apache.flink.runtime.rest.messages.job.JobVerticesInfo;
import org.apache.flink.runtime.rest.messages.job.metrics.IOMetricsInfo;
import org.apache.flink.runtime.scheduler.ExecutionGraphInfo;
import org.apache.flink.runtime.webmonitor.RestfulGateway;
import org.apache.flink.runtime.webmonitor.history.ArchivedJson;
import org.apache.flink.runtime.webmonitor.history.JsonArchivist;
import org.apache.flink.runtime.webmonitor.retriever.GatewayRetriever;
import org.apache.flink.util.Preconditions;

import org.apache.flink.shaded.netty4.io.netty.handler.codec.http.HttpResponseStatus;

import javax.annotation.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * A request handler that provides the details of a job all vertex,use ExecutionGraphInfo to draw it
 * including id, topology-id,name,parallelism and all subtask_metrics in map operators have
 * vertex_id, operator_id, name, inputs, metric_name
 */
public class JobVerticesInfoHandler
        extends AbstractExecutionGraphHandler<JobVerticesInfo, JobMessageParameters>
        implements JsonArchivist {
    private final MetricFetcher metricFetcher;

    public JobVerticesInfoHandler(
            GatewayRetriever<? extends RestfulGateway> leaderRetriever,
            Time timeout,
            Map<String, String> responseHeaders,
            MessageHeaders<EmptyRequestBody, JobVerticesInfo, JobMessageParameters> messageHeaders,
            ExecutionGraphCache executionGraphCache,
            Executor executor,
            MetricFetcher metricFetcher) {
        super(
                leaderRetriever,
                timeout,
                responseHeaders,
                messageHeaders,
                executionGraphCache,
                executor);

        this.metricFetcher = Preconditions.checkNotNull(metricFetcher);
    }

    protected JobVerticesInfo handleRequest(
            HandlerRequest<EmptyRequestBody> request, ExecutionGraphInfo executionGraphInfo)
            throws RestHandlerException {
        return createJobVerticesInfo(executionGraphInfo, this.metricFetcher);
    }

    @Override
    public Collection<ArchivedJson> archiveJsonWithPath(ExecutionGraphInfo executionGraphInfo)
            throws IOException, RestHandlerException {
        JobVerticesInfo jobVerticesInfo =
                createJobVerticesInfo(executionGraphInfo, this.metricFetcher);

        String path =
                getMessageHeaders()
                        .getTargetRestEndpointURL()
                        .replace(
                                ":jobid",
                                executionGraphInfo
                                        .getArchivedExecutionGraph()
                                        .getJobID()
                                        .toString());
        return Collections.singleton(new ArchivedJson(path, jobVerticesInfo));
    }

    private static JobVerticesInfo createJobVerticesInfo(
            ExecutionGraphInfo executionGraphInfo, @Nullable MetricFetcher metricFetcher)
            throws RestHandlerException {
        ArchivedExecutionGraph archivedExecutionGraph =
                executionGraphInfo.getArchivedExecutionGraph();

        Collection<JobVerticesInfo.JobVertex> jobVertices =
                new ArrayList<>(archivedExecutionGraph.getAllVertices().size());
        Collection<JobVerticesInfo.JobOperator> jobOperators = new ArrayList<>();
        metricFetcher.update();
        int topologyID = 0;

        for (AccessExecutionJobVertex accessExecutionJobVertex :
                archivedExecutionGraph.getVerticesTopologically()) {

            JobVerticesInfo.JobVertex jobVertex =
                    createJobVertex(
                            accessExecutionJobVertex,
                            archivedExecutionGraph.getJobID(),
                            topologyID,
                            metricFetcher,
                            jobOperators);

            jobVertices.add(jobVertex);
            topologyID++;
        }
        return new JobVerticesInfo(jobVertices, jobOperators);
    }

    private static JobVerticesInfo.JobVertex createJobVertex(
            AccessExecutionJobVertex ejv,
            JobID jobId,
            int topologyId,
            MetricFetcher metricFetcher,
            Collection<JobVerticesInfo.JobOperator> jobOperators)
            throws RestHandlerException {
        Collection<Map<String, String>> subTaskMetrics = new ArrayList<>();
        MutableIOMetrics counts = new MutableIOMetrics();
        AccessExecutionVertex[] arrayOfAccessExecutionVertex;
        int i;
        byte b;
        for (arrayOfAccessExecutionVertex = ejv.getTaskVertices(),
                        i = arrayOfAccessExecutionVertex.length,
                        b = 0;
                b < i; ) {
            AccessExecutionVertex vertex = arrayOfAccessExecutionVertex[b];

            MetricStore.ComponentMetricStore subTaskMetric =
                    metricFetcher
                            .getMetricStore()
                            .getSubtaskMetricStore(
                                    jobId.toString(),
                                    ejv.getJobVertexId().toString(),
                                    vertex.getCurrentExecutionAttempt().getParallelSubtaskIndex());
            counts.addIOMetrics(
                    vertex.getCurrentExecutionAttempt(),
                    metricFetcher,
                    jobId.toString(),
                    ejv.getJobVertexId().toString());
            if (subTaskMetric != null) {
                subTaskMetrics.add(subTaskMetric.getMetrics());

                b++;
            }
        }

        List<OperatorDescriptor> operatorDescriptors = null;
        try {
            operatorDescriptors = OperatorDescriptorUtils.getJobVertexOperatorDescriptors(ejv);
        } catch (Throwable e) {
            throw new RestHandlerException(
                    String.format(
                            "Failed to get operator for vertex %s.",
                            ejv.getJobVertexId().toString()),
                    HttpResponseStatus.INTERNAL_SERVER_ERROR,
                    e);
        }

        for (OperatorDescriptor od : operatorDescriptors) {
            JobVerticesInfo.JobOperator jobOperator = createJobOperator(ejv.getJobVertexId(), od);
            jobOperators.add(jobOperator);
        }
        IOMetricsInfo jobVertexMetrics = new IOMetricsInfo(counts);
        return new JobVerticesInfo.JobVertex(
                ejv.getJobVertexId(),
                topologyId,
                ejv.getName(),
                ejv.getParallelism(),
                subTaskMetrics,
                jobVertexMetrics);
    }

    private static JobVerticesInfo.JobOperator createJobOperator(
            JobVertexID jobVertexID, OperatorDescriptor operatorDescriptor) {
        List<OperatorEdgeDescriptor> inputs = operatorDescriptor.getInputs();

        Collection<JobVerticesInfo.OperatorEdgeInfo> operatorEdfInfos =
                new ArrayList<>(inputs.size());
        for (OperatorEdgeDescriptor oed : inputs) {
            operatorEdfInfos.add(
                    new JobVerticesInfo.OperatorEdgeInfo(
                            oed.getSourceOperator(),
                            oed.getPartitionerDescriptor(),
                            oed.getTypeNumber()));
        }
        return new JobVerticesInfo.JobOperator(
                jobVertexID,
                operatorDescriptor.getOperatorID(),
                operatorDescriptor.getOperatorName(),
                operatorEdfInfos,
                operatorDescriptor.getOperatorMetricName());
    }
}
