//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.apache.flink.runtime.rest.messages.job;

import org.apache.flink.runtime.jobgraph.JobVertexID;
import org.apache.flink.runtime.jobgraph.OperatorID;
import org.apache.flink.runtime.rest.messages.ResponseBody;
import org.apache.flink.runtime.rest.messages.job.metrics.IOMetricsInfo;
import org.apache.flink.runtime.rest.messages.json.JobVertexIDDeserializer;
import org.apache.flink.runtime.rest.messages.json.JobVertexIDSerializer;
import org.apache.flink.runtime.rest.messages.json.JobVertexOperatorIDDeserializer;
import org.apache.flink.runtime.rest.messages.json.JobVertexOperatorIDSerializer;
import org.apache.flink.util.Preconditions;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonCreator;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

public class JobVerticesInfo implements ResponseBody {
    public static final String FIELD_NAME_JOB_VERTICES = "vertices";
    public static final String FIELD_NAME_JOB_OPERATOR = "operators";

    @JsonProperty(FIELD_NAME_JOB_VERTICES)
    private final Collection<JobVertex> jobVertices;

    @JsonProperty(FIELD_NAME_JOB_OPERATOR)
    private final Collection<JobOperator> jobOperators;

    @JsonCreator
    public JobVerticesInfo(
            @JsonProperty(FIELD_NAME_JOB_VERTICES) Collection<JobVertex> jobVertices,
            @JsonProperty(FIELD_NAME_JOB_OPERATOR) Collection<JobOperator> jobOperators) {
        this.jobVertices = Preconditions.checkNotNull(jobVertices);
        this.jobOperators = Preconditions.checkNotNull(jobOperators);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            JobVerticesInfo that = (JobVerticesInfo) o;
            return Objects.equals(this.jobVertices, that.jobVertices)
                    && Objects.equals(this.jobOperators, that.jobOperators);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(this.jobVertices, this.jobOperators);
    }

    @JsonIgnore
    public Collection<JobVertex> getJobVertices() {
        return this.jobVertices;
    }

    @JsonIgnore
    public Collection<JobOperator> getJobOperators() {
        return this.jobOperators;
    }

    public static final class OperatorEdgeInfo {
        public static final String FIELD_NAME_VERTEX_OPERATOR_EDG_TARGET_ID = "operator_id";
        public static final String FIELD_NAME_VERTEX_OPERATOR_EDG_PARTITIONER = "partitioner";
        public static final String FIELD_NAME_VERTEX_OPERATOR_EDG_TYPE_NUMBER = "type_number";

        @JsonProperty(FIELD_NAME_VERTEX_OPERATOR_EDG_TARGET_ID)
        @JsonSerialize(using = JobVertexOperatorIDSerializer.class)
        private final OperatorID jobVertexOperatorID;

        @JsonProperty(FIELD_NAME_VERTEX_OPERATOR_EDG_PARTITIONER)
        private final String partitioner;

        @JsonProperty(FIELD_NAME_VERTEX_OPERATOR_EDG_TYPE_NUMBER)
        private final int typeNumber;

        @JsonCreator
        public OperatorEdgeInfo(
                @JsonDeserialize(using = JobVertexOperatorIDDeserializer.class)
                        @JsonProperty(FIELD_NAME_VERTEX_OPERATOR_EDG_TARGET_ID)
                        OperatorID jobVertexOperatorID,
                @JsonProperty(FIELD_NAME_VERTEX_OPERATOR_EDG_PARTITIONER) String partitioner,
                @JsonProperty(FIELD_NAME_VERTEX_OPERATOR_EDG_TYPE_NUMBER) int typeNumber) {
            this.jobVertexOperatorID = Preconditions.checkNotNull(jobVertexOperatorID);
            this.partitioner = Preconditions.checkNotNull(partitioner);
            this.typeNumber = typeNumber;
        }

        @JsonIgnore
        public OperatorID getJobVertexOperatorID() {
            return this.jobVertexOperatorID;
        }

        @JsonIgnore
        public String getPartitioner() {
            return this.partitioner;
        }

        @JsonIgnore
        public int getTypeNumber() {
            return this.typeNumber;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            } else if (o != null && this.getClass() == o.getClass()) {
                OperatorEdgeInfo that = (OperatorEdgeInfo) o;
                return Objects.equals(this.jobVertexOperatorID, that.jobVertexOperatorID)
                        && Objects.equals(this.partitioner, that.partitioner)
                        && Objects.equals(this.typeNumber, that.typeNumber);
            } else {
                return false;
            }
        }

        public int hashCode() {
            return Objects.hash(this.jobVertexOperatorID, this.partitioner, this.typeNumber);
        }
    }

    public static final class JobOperator {
        public static final String FIELD_NAME_VERTEX_VERTEX_ID = "vertex_id";
        public static final String FIELD_NAME_VERTEX_OPERATOR_ID = "operator_id";
        public static final String FIELD_NAME_VERTEX_OPERATOR_NAME = "name";
        public static final String FIELD_NAME_VERTEX_OPERATOR_INPUTS = "inputs";
        public static final String FIELD_NAME_VERTEX_OPERATOR_METRIC_NAME = "metric_name";

        @JsonProperty(FIELD_NAME_VERTEX_VERTEX_ID)
        @JsonSerialize(using = JobVertexIDSerializer.class)
        private final JobVertexID jobVertexID;

        @JsonProperty(FIELD_NAME_VERTEX_OPERATOR_ID)
        @JsonSerialize(using = JobVertexOperatorIDSerializer.class)
        private final OperatorID jobVertexOperatorID;

        @JsonProperty(FIELD_NAME_VERTEX_OPERATOR_NAME)
        private final String name;

        @JsonProperty(FIELD_NAME_VERTEX_OPERATOR_INPUTS)
        private final Collection<OperatorEdgeInfo> inputs;

        @JsonProperty(FIELD_NAME_VERTEX_OPERATOR_METRIC_NAME)
        private final String metricName;

        @JsonCreator
        public JobOperator(
                @JsonDeserialize(using = JobVertexIDDeserializer.class)
                        @JsonProperty(FIELD_NAME_VERTEX_VERTEX_ID)
                        JobVertexID jobVertexID,
                @JsonDeserialize(using = JobVertexOperatorIDDeserializer.class)
                        @JsonProperty(FIELD_NAME_VERTEX_OPERATOR_ID)
                        OperatorID jobVertexOperatorID,
                @JsonProperty(FIELD_NAME_VERTEX_OPERATOR_NAME) String name,
                @JsonProperty(FIELD_NAME_VERTEX_OPERATOR_INPUTS)
                        Collection<OperatorEdgeInfo> inputs,
                @JsonProperty(FIELD_NAME_VERTEX_OPERATOR_METRIC_NAME) String metricName) {
            this.jobVertexOperatorID = Preconditions.checkNotNull(jobVertexOperatorID);
            this.name = Preconditions.checkNotNull(name);
            this.inputs = inputs;
            this.metricName = metricName;
            this.jobVertexID = jobVertexID;
        }

        @JsonIgnore
        public JobVertexID getJobVertexID() {
            return this.jobVertexID;
        }

        @JsonIgnore
        public OperatorID getJobVertexOperatorID() {
            return this.jobVertexOperatorID;
        }

        @JsonIgnore
        public String getName() {
            return this.name;
        }

        @JsonIgnore
        public Collection<OperatorEdgeInfo> getInputs() {
            return this.inputs;
        }

        @JsonIgnore
        public String getMetricName() {
            return this.metricName;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            } else if (o != null && this.getClass() == o.getClass()) {
                JobOperator that = (JobOperator) o;
                return Objects.equals(this.jobVertexID, that.jobVertexID)
                        && Objects.equals(this.jobVertexOperatorID, that.jobVertexOperatorID)
                        && Objects.equals(this.name, that.name)
                        && Objects.equals(this.inputs, that.inputs)
                        && Objects.equals(this.metricName, that.metricName);
            } else {
                return false;
            }
        }

        public int hashCode() {
            return Objects.hash(
                    this.jobVertexID,
                    this.jobVertexOperatorID,
                    this.name,
                    this.inputs,
                    this.metricName);
        }
    }

    public static final class JobVertex {
        public static final String FIELD_NAME_JOB_VERTEX_ID = "id";
        public static final String FIELD_NAME_JOB_VERTEX_NAME = "name";
        public static final String FIELD_NAME_TOPOLOGY_ID = "topology-id";
        public static final String FIELD_NAME_JOB_VERTEX_PARALLELISM = "parallelism";
        public static final String FIELD_NAME_JOB_VERTEX_SUBTASK_METRICS = "subtask_metrics";
        public static final String FIELD_NAME_JOB_VERTEX_METRICS = "metrics";

        @JsonProperty(FIELD_NAME_JOB_VERTEX_ID)
        @JsonSerialize(using = JobVertexIDSerializer.class)
        private final JobVertexID jobVertexID;

        @JsonProperty(FIELD_NAME_TOPOLOGY_ID)
        private final int topologyID;

        @JsonProperty(FIELD_NAME_JOB_VERTEX_NAME)
        private final String name;

        @JsonProperty(FIELD_NAME_JOB_VERTEX_PARALLELISM)
        private final int parallelism;

        @JsonProperty(FIELD_NAME_JOB_VERTEX_SUBTASK_METRICS)
        private final Collection<Map<String, String>> jobVertexSubTaskMetrics;

        @JsonProperty(FIELD_NAME_JOB_VERTEX_METRICS)
        private final IOMetricsInfo jobVertexMetrics;

        @JsonCreator
        public JobVertex(
                @JsonDeserialize(using = JobVertexIDDeserializer.class)
                        @JsonProperty(FIELD_NAME_JOB_VERTEX_ID)
                        JobVertexID jobVertexID,
                @JsonProperty(FIELD_NAME_TOPOLOGY_ID) int topologyID,
                @JsonProperty(FIELD_NAME_JOB_VERTEX_NAME) String name,
                @JsonProperty(FIELD_NAME_JOB_VERTEX_PARALLELISM) int parallelism,
                @JsonProperty(FIELD_NAME_JOB_VERTEX_SUBTASK_METRICS)
                        Collection<Map<String, String>> jobVertexSubTaskMetrics,
                @JsonProperty(FIELD_NAME_JOB_VERTEX_METRICS) IOMetricsInfo jobVertexMetrics) {
            this.jobVertexID = Preconditions.checkNotNull(jobVertexID);
            this.topologyID = topologyID;
            this.name = Preconditions.checkNotNull(name);
            this.parallelism = parallelism;
            this.jobVertexSubTaskMetrics = Preconditions.checkNotNull(jobVertexSubTaskMetrics);
            this.jobVertexMetrics = Preconditions.checkNotNull(jobVertexMetrics);
        }

        @JsonIgnore
        public JobVertexID getJobVertexID() {
            return this.jobVertexID;
        }

        @JsonIgnore
        public int getTopologyID() {
            return this.topologyID;
        }

        @JsonIgnore
        public String getName() {
            return this.name;
        }

        @JsonIgnore
        public int getParallelism() {
            return this.parallelism;
        }

        @JsonIgnore
        public Collection<Map<String, String>> getJobVertexSubTaskMetrics() {
            return this.jobVertexSubTaskMetrics;
        }

        @JsonIgnore
        public IOMetricsInfo getJobVertexMetrics() {
            return this.jobVertexMetrics;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            } else if (o != null && this.getClass() == o.getClass()) {
                JobVertex that = (JobVertex) o;
                return Objects.equals(this.jobVertexID, that.jobVertexID)
                        && this.topologyID == that.topologyID
                        && Objects.equals(this.name, that.name)
                        && this.parallelism == that.parallelism
                        && Objects.equals(
                                this.jobVertexSubTaskMetrics, that.jobVertexSubTaskMetrics)
                        && Objects.equals(this.jobVertexMetrics, that.jobVertexMetrics);
            } else {
                return false;
            }
        }

        public int hashCode() {
            return Objects.hash(
                    this.jobVertexID,
                    this.topologyID,
                    this.name,
                    this.parallelism,
                    this.jobVertexSubTaskMetrics,
                    this.jobVertexMetrics);
        }
    }
}
