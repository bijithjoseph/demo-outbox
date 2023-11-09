package com.transaction.outbox.demooutbox.spring;

import com.transaction.outbox.demooutbox.spring.aop.MethodProxyConfiguration;
import org.springframework.context.annotation.AutoProxyRegistrar;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

public class ConfigurationSelector implements ImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return new String[]{AutoProxyRegistrar.class.getName(), MethodProxyConfiguration.class.getName()};
    }
}
