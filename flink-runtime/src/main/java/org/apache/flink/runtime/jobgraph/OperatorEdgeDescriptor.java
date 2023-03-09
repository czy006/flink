package org.apache.flink.runtime.jobgraph;

import org.apache.flink.util.Preconditions;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

public class OperatorEdgeDescriptor implements Serializable {
    private final OperatorID sourceOperator;
    private final OperatorID targetOperator;
    private final int typeNumber;
    private final String partitionerDescriptor;

    public OperatorEdgeDescriptor(
            OperatorID sourceOperator,
            OperatorID targetOperator,
            int typeNumber,
            String partitionerDescriptor) {
        this.sourceOperator = (OperatorID) Preconditions.checkNotNull(sourceOperator);
        this.targetOperator = (OperatorID) Preconditions.checkNotNull(targetOperator);
        this.typeNumber = typeNumber;
        this.partitionerDescriptor = (String) Preconditions.checkNotNull(partitionerDescriptor);
    }

    public OperatorID getSourceOperator() {
        return this.sourceOperator;
    }

    public OperatorID getTargetOperator() {
        return this.targetOperator;
    }

    public int getTypeNumber() {
        return this.typeNumber;
    }

    public String getPartitionerDescriptor() {
        return this.partitionerDescriptor;
    }

    public String toString() {
        return (new ToStringBuilder(this))
                .append("sourceOperator", this.sourceOperator)
                .append("targetOperator", this.targetOperator)
                .append("typeNumber", this.typeNumber)
                .append("partitionerDescriptor", this.partitionerDescriptor)
                .toString();
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else {
            OperatorEdgeDescriptor descriptor = (OperatorEdgeDescriptor) obj;
            return this.sourceOperator.equals(descriptor.sourceOperator)
                    && this.targetOperator.equals(descriptor.targetOperator)
                    && this.typeNumber == descriptor.typeNumber
                    && this.partitionerDescriptor.equalsIgnoreCase(
                            descriptor.partitionerDescriptor);
        }
    }
}
