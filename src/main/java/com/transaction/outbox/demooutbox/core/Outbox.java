package com.transaction.outbox.demooutbox.core;

import com.transaction.outbox.demooutbox.core.domain.TransactionOutBoxEntry;
import com.transaction.outbox.demooutbox.core.exception.NoTransactionActiveException;

public interface Outbox {
    void scheduleImmediately(TransactionOutBoxEntry transactionOutBoxEntry) throws NoTransactionActiveException;
    void poll();
}
