package org.apache.flink.streaming.api.graph;

import org.apache.flink.annotation.Internal;
import org.apache.flink.runtime.jobgraph.JobVertex;
import org.apache.flink.runtime.jobgraph.OperatorDescriptor;
import org.apache.flink.runtime.jobgraph.OperatorEdgeDescriptor;
import org.apache.flink.runtime.jobgraph.OperatorID;
import org.apache.flink.streaming.runtime.tasks.StreamTaskException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

@Internal
public class OperatorDescriptorUtils {
    public static void setOperatorDescriptors(
            Map<Integer, JobVertex> jobVertices,
            Function<Integer, OperatorID> operatorIDRetriever,
            Function<Integer, Set<Integer>> vertexOperatorsRetriever,
            Function<Integer, StreamNode> streamNodeRetriever) {
        for (Map.Entry<Integer, JobVertex> entry : jobVertices.entrySet()) {
            int headOperatorNodeId = ((Integer) entry.getKey()).intValue();
            JobVertex jobVertex = entry.getValue();
            Set<Integer> operatorNodeIds = new HashSet<>();
            operatorNodeIds.add(Integer.valueOf(headOperatorNodeId));
            operatorNodeIds.addAll(
                    vertexOperatorsRetriever.apply(Integer.valueOf(headOperatorNodeId)));
            List<OperatorDescriptor> operatorDescriptorList = new ArrayList<>();
            for (Integer operatorNodeId : operatorNodeIds) {
                StreamNode operatorNode = streamNodeRetriever.apply(operatorNodeId);
                OperatorID operatorID = operatorIDRetriever.apply(operatorNodeId);
                OperatorDescriptor operatorDescriptor =
                        new OperatorDescriptor(
                                operatorNode.getOperatorName(), operatorID, operatorNodeId);
                for (StreamEdge streamEdge : operatorNode.getInEdges()) {
                    OperatorEdgeDescriptor edgeDescriptor =
                            new OperatorEdgeDescriptor(
                                    operatorIDRetriever.apply(
                                            Integer.valueOf(streamEdge.getSourceId())),
                                    operatorID,
                                    streamEdge.getTypeNumber(),
                                    (streamEdge.getPartitioner() == null)
                                            ? "null"
                                            : streamEdge.getPartitioner().toString());
                    operatorDescriptor.addInput(edgeDescriptor);
                }
                operatorDescriptorList.add(operatorDescriptor);
            }
            try {
                org.apache.flink.runtime.jobgraph.OperatorDescriptorUtils.writeOperatorDescriptors(
                        jobVertex.getConfiguration(), operatorDescriptorList);
            } catch (Exception e) {
                throw new StreamTaskException(
                        "Could not write operator descriptors into job vertex config.", e);
            }
        }
    }
}
