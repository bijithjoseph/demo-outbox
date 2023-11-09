package com.transaction.outbox.demooutbox.core;

import org.springframework.transaction.annotation.Transactional;


public interface OutboxCleaner {
    @Transactional
    void clear(String id);
}
