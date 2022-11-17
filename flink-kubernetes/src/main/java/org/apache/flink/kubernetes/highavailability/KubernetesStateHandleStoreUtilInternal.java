package org.apache.flink.kubernetes.highavailability;

import org.apache.flink.annotation.VisibleForTesting;
import org.apache.flink.runtime.checkpoint.CompletedCheckpoint;
import org.apache.flink.runtime.checkpoint.Snapshot;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class KubernetesStateHandleStoreUtilInternal {

    private static final Logger LOG =
            LoggerFactory.getLogger(KubernetesStateHandleStoreUtilInternal.class);
    public static final String ADDITIONAL_PROPERTIES_SUFFIX = "-additional-properties";
    private static final ObjectMapper mapper = new ObjectMapper();

    public KubernetesStateHandleStoreUtilInternal() {}

    public static <T> void trySetAdditionalProperties(
            String key, T state, Map<String, String> data) {
        if (state instanceof CompletedCheckpoint) {
            CompletedCheckpoint completedCheckpoint = (CompletedCheckpoint) state;
            CheckpointAdditionalProperties checkpointInfo = new CheckpointAdditionalProperties();
            String checkpointType =
                    completedCheckpoint.getProperties().getCheckpointType().isSavepoint()
                            ? Snapshot.SnapshotType.SAVEPOINT.getDescription()
                            : Snapshot.SnapshotType.CHECKPOINT.getDescription();
            checkpointInfo.setCheckpointType(checkpointType);
            checkpointInfo.setExternalPointer(completedCheckpoint.getExternalPointer());

            try {
                data.put(
                        key + ADDITIONAL_PROPERTIES_SUFFIX,
                        mapper.writeValueAsString(checkpointInfo));
            } catch (JsonProcessingException var7) {
                LOG.warn("Failed to get additional properties", var7);
            }
        }
    }

    public static void tryRemoveAdditionalProperties(String key, Map<String, String> data) {
        data.remove(key + ADDITIONAL_PROPERTIES_SUFFIX);
    }

    @VisibleForTesting
    public static class CheckpointAdditionalProperties {
        String checkpointType;
        String externalPointer;

        public CheckpointAdditionalProperties() {}

        public String getCheckpointType() {
            return this.checkpointType;
        }

        public void setCheckpointType(String checkpointType) {
            this.checkpointType = checkpointType;
        }

        public String getExternalPointer() {
            return this.externalPointer;
        }

        public void setExternalPointer(String externalPointer) {
            this.externalPointer = externalPointer;
        }
    }
}
