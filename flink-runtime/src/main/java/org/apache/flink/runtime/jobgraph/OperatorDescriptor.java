package org.apache.flink.runtime.jobgraph;

import org.apache.flink.runtime.metrics.dump.MetricQueryService;
import org.apache.flink.util.Preconditions;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class OperatorDescriptor implements Serializable {
    public static final int METRICS_OPERATOR_NAME_MAX_LENGTH = 80;
    private final String operatorName;
    private final OperatorID operatorID;
    private final String operatorMetricName;
    private final Integer nodeId;
    private final List<OperatorEdgeDescriptor> inputs = new ArrayList();

    public OperatorDescriptor(String operatorName, OperatorID operatorID, Integer nodeId) {
        this.operatorName = (String) Preconditions.checkNotNull(operatorName);
        this.operatorID = (OperatorID) Preconditions.checkNotNull(operatorID);
        this.operatorMetricName = this.nameToMetricName(operatorName);
        this.nodeId = (Integer) Preconditions.checkNotNull(nodeId);
    }

    private String nameToMetricName(String name) {
        String metricName = (String) Preconditions.checkNotNull(name);
        if (metricName.length() > METRICS_OPERATOR_NAME_MAX_LENGTH) {
            metricName = metricName.substring(0, METRICS_OPERATOR_NAME_MAX_LENGTH);
        }

        metricName = MetricQueryService.FILTER.filterCharacters(metricName);
        return metricName;
    }

    public void addInput(OperatorEdgeDescriptor operatorEdgeDescriptor) {
        this.inputs.add(operatorEdgeDescriptor);
    }

    public String getOperatorName() {
        return this.operatorName;
    }

    public OperatorID getOperatorID() {
        return this.operatorID;
    }

    public String getOperatorMetricName() {
        return this.operatorMetricName;
    }

    public List<OperatorEdgeDescriptor> getInputs() {
        return this.inputs;
    }

    public Integer getNodeId() {
        return this.nodeId;
    }

    public String toString() {
        return (new ToStringBuilder(this))
                .append("operatorName", this.operatorName)
                .append("operatorID", this.operatorID)
                .append("inputs", this.inputs)
                .append("metricName", this.operatorMetricName)
                .append("nodeId", this.nodeId)
                .toString();
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else {
            OperatorDescriptor descriptor = (OperatorDescriptor) obj;
            return this.operatorID.equals(descriptor.operatorID)
                    && this.operatorName.equals(descriptor.operatorName)
                    && this.nodeId.equals(descriptor.nodeId)
                    && this.inputs.equals(descriptor.inputs);
        }
    }
}
