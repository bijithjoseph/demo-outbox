package com.transaction.outbox.demooutbox.api;

import com.transaction.outbox.demooutbox.core.domain.Invocation;
import org.springframework.beans.factory.ObjectProvider;

import java.util.Optional;

public interface InvocationInstantiable {
    Optional<Object> instantiate(Invocation invocation);
}
