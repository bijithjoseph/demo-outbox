package com.transaction.outbox.demooutbox.core;

import com.transaction.outbox.demooutbox.core.dao.DataAccessor;
import com.transaction.outbox.demooutbox.core.domain.TransactionOutBoxEntry;
import com.transaction.outbox.demooutbox.core.exception.NoTransactionActiveException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

@Slf4j
public class DefaultOutbox implements Outbox {
    private final ExecutorService executorService;
    private final DataAccessor dataAccessor;
    private final OutboxCleaner outboxCleaner;

    public DefaultOutbox(ExecutorService executorService, DataAccessor dataAccessor, OutboxCleaner outboxCleaner) {
        this.executorService = executorService;
        this.dataAccessor = dataAccessor;
        this.outboxCleaner = outboxCleaner;
    }

    @Override
    public void scheduleImmediately(TransactionOutBoxEntry transactionOutBoxEntry) throws NoTransactionActiveException {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new NoTransactionActiveException();
        }
        log.debug("Saving entry:{}", transactionOutBoxEntry);
        dataAccessor.save(transactionOutBoxEntry);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                log.info("Post Commit transaction, submitting to executor for id: {}", transactionOutBoxEntry.getId());
                executorService.submit(() -> outboxCleaner.clear(transactionOutBoxEntry.getId()));
                log.debug("Submitted for id: {}", transactionOutBoxEntry.getId());
            }
        });
        log.debug("Scheduled {} for running after transaction commit", transactionOutBoxEntry.description());
    }

    @Override
    public void poll() {
        log.info("polling started in async");
        dataAccessor.fetchUnprocessedInBatchAndCallback(10, entries ->
                Optional.ofNullable(entries).orElse(Collections.emptyList()).forEach(entry -> {
                    log.debug("Polled entry: {}", entry);
                    if (entry.getNextAttemptTime() != null && Instant.now().isBefore(entry.getNextAttemptTime())) {
                        log.debug("Time has not come to process: {}", entry);
                        return;
                    }
                    log.info("Polled entry, submitting to executor for id: {}", entry.getId());
                    executorService.submit(() -> outboxCleaner.clear(entry.getId()));
                    log.debug("polled entry, submitted for id: {}", entry.getId());
                }));
        dataAccessor.deleteProcessedEntryByApplication();

    }
}
