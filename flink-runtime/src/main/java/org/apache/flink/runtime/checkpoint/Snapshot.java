package org.apache.flink.runtime.checkpoint;

import org.apache.flink.util.Preconditions;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonCreator;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

public class Snapshot {

    private static final String FIELD_NAME_CHECKPOINT_ID = "checkpointId";
    private static final String FIELD_NAME_CHECKPOINT_TYPE = "checkpointType";
    private static final String FIELD_NAME_EXTERNAL_POINTER = "externalPointer";
    private final long checkpointId;
    private final String checkpointType;
    private final String externalPointer;

    @JsonCreator
    public Snapshot(
            @JsonProperty(FIELD_NAME_CHECKPOINT_ID) long checkpointId,
            @JsonProperty(FIELD_NAME_CHECKPOINT_TYPE) String checkpointType,
            @JsonProperty(FIELD_NAME_EXTERNAL_POINTER) String externalPointer) {
        this.checkpointId = Preconditions.checkNotNull(checkpointId);
        this.checkpointType = Preconditions.checkNotNull(checkpointType.toUpperCase());
        this.externalPointer = Preconditions.checkNotNull(externalPointer);
    }

    @JsonProperty(FIELD_NAME_CHECKPOINT_ID)
    public long getCheckpointId() {
        return this.checkpointId;
    }

    @JsonProperty(FIELD_NAME_CHECKPOINT_TYPE)
    public String getCheckpointType() {
        return this.checkpointType;
    }

    @JsonProperty(FIELD_NAME_EXTERNAL_POINTER)
    public String getExternalPointer() {
        return this.externalPointer;
    }

    public static Snapshot fromCheckpoint(CompletedCheckpointStats checkpoint) {
        return new Snapshot(
                checkpoint.getCheckpointId(),
                checkpoint.getProperties().isSavepoint()
                        ? Snapshot.SnapshotType.SAVEPOINT.getDescription()
                        : Snapshot.SnapshotType.CHECKPOINT.getDescription(),
                checkpoint.getExternalPath());
    }

    public static Snapshot fromRestoredCheckpoint(RestoredCheckpointStats restoredCheckpoint) {
        return new Snapshot(
                restoredCheckpoint.getCheckpointId(),
                restoredCheckpoint.getProperties().isSavepoint()
                        ? Snapshot.SnapshotType.SAVEPOINT.getDescription()
                        : Snapshot.SnapshotType.CHECKPOINT.getDescription(),
                restoredCheckpoint.getExternalPath());
    }

    public static enum SnapshotType {
        CHECKPOINT("CHECKPOINT"),
        SAVEPOINT("SAVEPOINT");

        private final String description;

        private SnapshotType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return this.description;
        }
    }
}
