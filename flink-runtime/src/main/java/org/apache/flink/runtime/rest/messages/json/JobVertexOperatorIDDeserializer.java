package org.apache.flink.runtime.rest.messages.json;

import org.apache.flink.runtime.jobgraph.OperatorID;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.JsonParser;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.DeserializationContext;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class JobVertexOperatorIDDeserializer extends StdDeserializer<OperatorID> {
    private static final long serialVersionUID = 4000884101262350161L;

    protected JobVertexOperatorIDDeserializer() {
        super(OperatorID.class);
    }

    @Override
    public OperatorID deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return OperatorID.fromHexString(p.getValueAsString());
    }
}
