package com.transaction.outbox.demooutbox.core.dao;

import com.transaction.outbox.demooutbox.core.domain.TransactionOutBoxEntry;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public interface DataAccessor {
    TransactionOutBoxEntry save(TransactionOutBoxEntry entry);
    int deleteProcessedEntry(String id);
    Optional<TransactionOutBoxEntry> findByIdWithLock(String id);
    void fetchUnprocessedInBatchAndCallback(int batchSize, Consumer<List<TransactionOutBoxEntry>> consumer);
    void deleteProcessedEntryByApplication();
    void deleteEntryById(String id);
}
