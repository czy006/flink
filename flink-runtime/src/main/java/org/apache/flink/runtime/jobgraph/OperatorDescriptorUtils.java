package org.apache.flink.runtime.jobgraph;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.executiongraph.AccessExecutionJobVertex;
import org.apache.flink.util.InstantiationUtil;

import java.io.IOException;
import java.util.List;

public class OperatorDescriptorUtils {
    private static final String OPERATOR_DESCRIPTORS = "operatorDescriptors";

    private OperatorDescriptorUtils() {}

    public static void writeOperatorDescriptors(
            Configuration configuration, List<OperatorDescriptor> operatorDescriptors)
            throws IOException {
        configuration.setBytes(
                OPERATOR_DESCRIPTORS, InstantiationUtil.serializeObject(operatorDescriptors));
    }

    public static List<OperatorDescriptor> getOperatorDescriptors(Configuration configuration)
            throws IOException, ClassNotFoundException {
        byte[] bytes = configuration.getBytes(OPERATOR_DESCRIPTORS, null);
        return InstantiationUtil.deserializeObject(
                bytes, OperatorDescriptorUtils.class.getClassLoader());
    }

    public static List<OperatorDescriptor> getJobVertexOperatorDescriptors(
            AccessExecutionJobVertex accessExecutionJobVertex)
            throws IOException, ClassNotFoundException {
        return getOperatorDescriptors(accessExecutionJobVertex.getConfiguration());
    }
}
