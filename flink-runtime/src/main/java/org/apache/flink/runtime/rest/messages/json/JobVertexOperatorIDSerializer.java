package org.apache.flink.runtime.rest.messages.json;

import org.apache.flink.runtime.jobgraph.OperatorID;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.JsonGenerator;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.SerializerProvider;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class JobVertexOperatorIDSerializer extends StdSerializer<OperatorID> {
    private static final long serialVersionUID = 3337595128571667113L;

    public JobVertexOperatorIDSerializer() {
        super(OperatorID.class);
    }

    @Override
    public void serialize(OperatorID value, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        gen.writeString(value.toString());
    }
}
