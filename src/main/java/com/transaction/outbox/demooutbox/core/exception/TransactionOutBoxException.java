package com.transaction.outbox.demooutbox.core.exception;

public class TransactionOutBoxException extends RuntimeException {

    public TransactionOutBoxException(String errorInDeserialization) {
        super(errorInDeserialization);
    }
    public TransactionOutBoxException(String errorInDeserialization, Throwable e) {
        super(errorInDeserialization, e);
    }
}
