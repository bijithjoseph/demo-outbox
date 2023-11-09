package com.transaction.outbox.demooutbox.core.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.transaction.outbox.demooutbox.core.domain.Invocation;
import com.transaction.outbox.demooutbox.core.domain.JacksonIgnoreAvroPropertiesMixIn;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import javax.transaction.TransactionalException;

@Slf4j
@Converter
public class InvocationConverter implements AttributeConverter<Invocation, String> {
    private ObjectMapper objectMapper = new ObjectMapper();
    public InvocationConverter(){
        objectMapper.addMixIn(org.apache.avro.specific.SpecificRecord.class,
                JacksonIgnoreAvroPropertiesMixIn.class);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    @Override
    public String convertToDatabaseColumn(Invocation invocation) {
        String valueAsString;
        try{
            valueAsString = objectMapper.writeValueAsString(invocation);
        } catch (JsonProcessingException e) {
            log.error("Error while serializing the invocation",e);
            throw new TransactionalException("Error while serializing the invocation",e);
        }
        return valueAsString;
    }

    @Override
    public Invocation convertToEntityAttribute(String s) {
        Invocation invocation;
        try {
            invocation = objectMapper.readValue(s, Invocation.class);
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            log.error("Error while de-serializing the invocation",e);
            throw new TransactionalException("Error while de-serializing the invocation",e);
        }
        return invocation;
    }
}
