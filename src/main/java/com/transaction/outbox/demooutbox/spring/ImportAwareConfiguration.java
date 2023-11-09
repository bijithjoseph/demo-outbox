package com.transaction.outbox.demooutbox.spring;

import com.transaction.outbox.demooutbox.annotation.EnableTransactionOutbox;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

public class ImportAwareConfiguration implements ImportAware {
    protected AnnotationAttributes annotationAttributes;

    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {
        this.annotationAttributes = AnnotationAttributes.fromMap(
                importMetadata.getAnnotationAttributes(EnableTransactionOutbox.class.getName(),
                        false));

    }

    protected int getOrder() {
        return annotationAttributes.getNumber("order");
    }
}
