package com.transaction.outbox.demooutbox.configuration;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class ApplicationConfiguration {
    @Value("${transaction.outbox.client}")
    private String tranOutboxClient;

}
