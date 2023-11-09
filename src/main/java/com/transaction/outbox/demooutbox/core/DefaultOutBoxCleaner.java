package com.transaction.outbox.demooutbox.core;

import com.transaction.outbox.demooutbox.api.InvocationInstantiable;
import com.transaction.outbox.demooutbox.core.dao.DataAccessor;
import com.transaction.outbox.demooutbox.core.domain.TransactionOutBoxEntry;
import com.transaction.outbox.demooutbox.core.exception.TransactionOutBoxException;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class DefaultOutBoxCleaner implements OutboxCleaner {
    private final DataAccessor dataAccessor;
    private final InvocationInstantiable proxyRipper;

    public DefaultOutBoxCleaner(DataAccessor dataAccessor, InvocationInstantiable invocationInstantiable) {
        this.dataAccessor = dataAccessor;
        this.proxyRipper = invocationInstantiable;
    }

    @Override
    public void clear(String id) {
        long startTime = System.currentTimeMillis();
        log.info("Clearing for entry id started: {}", id);
        Optional<TransactionOutBoxEntry> entryHolder = dataAccessor.findByIdWithLock(id);
        log.info("acquired locked? : {}", entryHolder.isPresent());
        entryHolder.ifPresent(
                entry -> {
                    final Optional<Object> instance = proxyRipper.instantiate(entry.getInvocation());
                    log.debug("Instantiated invocation target for proceeding : {}", instance);
                    if (instance.isPresent()) {
                        try {
                            entry.getInvocation().invoke(instance.get());
                            dataAccessor.deleteEntryById(entry.getId());
                            log.info("Entry with ID {} deleted successfully", id);
                        } catch (Exception e) {
                            log.error("Error in executing the task", e);
                            throw new TransactionOutBoxException("Exception while executing the actual target task", e);
                        }
                    }
                });
        long endTime = System.currentTimeMillis();
        log.info("Clearing completed for entry id:{}, in ms: {}", id, (endTime - startTime));
    }
}
