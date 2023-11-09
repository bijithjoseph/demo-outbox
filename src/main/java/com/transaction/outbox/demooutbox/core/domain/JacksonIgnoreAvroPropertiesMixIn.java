package com.transaction.outbox.demooutbox.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface JacksonIgnoreAvroPropertiesMixIn {
    @JsonIgnore
    org.apache.avro.Schema getSchema();
    @JsonIgnore
    org.apache.avro.specific.SpecificData getSpecificData();
}
