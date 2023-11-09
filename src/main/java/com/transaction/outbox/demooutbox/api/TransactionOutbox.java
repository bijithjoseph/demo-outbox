package com.transaction.outbox.demooutbox.api;

public interface TransactionOutbox<T> {
    @com.transaction.outbox.demooutbox.annotation.TransactionOutbox(nonTransactionalMethod = "execute")
    void executeInTransaction(T message);
    default void execute(T message){
        executeInTransaction(message);
    }
}
