package com.transaction.outbox.demooutbox.core.domain;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.transaction.outbox.demooutbox.core.exception.TransactionOutBoxException;

import java.io.IOException;

public class ArgumentHolderDeserializer extends StdDeserializer<ArgumentHolder> {

    public ArgumentHolderDeserializer() {
        this(null);
    }
    public ArgumentHolderDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public ArgumentHolder deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {

        ObjectMapper mapper = (ObjectMapper) jsonParser.getCodec();
        JsonNode tree = mapper.readTree(jsonParser);
        String argumentType = tree.get("at").asText();
        try {
            Class<?> type = Class.forName(argumentType);
            String argumentJson = tree.get("av").toString();

            Object argumentObject = mapper.readValue(argumentJson,type);

            return  ArgumentHolder.builder()
                    .argType(argumentType)
                    .arg(argumentObject)
                    .build();
        } catch (ClassNotFoundException e) {
            throw new TransactionOutBoxException("Error in Deserialization", e);
        }

    }
}
