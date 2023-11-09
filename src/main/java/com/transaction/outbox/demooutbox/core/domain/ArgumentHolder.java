package com.transaction.outbox.demooutbox.core.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonDeserialize(using = ArgumentHolderDeserializer.class)
public class ArgumentHolder {

    @JsonProperty("at")
    private String argType;

    @JsonProperty("av")
    private Object arg;
}
