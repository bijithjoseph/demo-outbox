package com.transaction.outbox.demooutbox.annotation;

import com.transaction.outbox.demooutbox.spring.ConfigurationSelector;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({ConfigurationSelector.class})
public @interface EnableTransactionOutbox {
    /** indicate the ordering of execution of the locking advisor whem multiple advisor area applied at specific jointpoint **/

    int order() default Ordered.LOWEST_PRECEDENCE;
}
