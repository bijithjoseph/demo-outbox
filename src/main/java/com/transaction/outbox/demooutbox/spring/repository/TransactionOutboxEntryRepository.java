package com.transaction.outbox.demooutbox.spring.repository;

import com.transaction.outbox.demooutbox.core.domain.TransactionOutBoxEntry;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;

import java.util.List;
import java.util.Optional;

import static org.hibernate.cfg.AvailableSettings.JPA_LOCK_TIMEOUT;

@Repository
public interface TransactionOutboxEntryRepository extends JpaRepository<TransactionOutBoxEntry, String> {
    String SKIP_LOCKED= "-2";
    @QueryHints({@QueryHint(name= JPA_LOCK_TIMEOUT,value = SKIP_LOCKED)})
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<TransactionOutBoxEntry> findById(@Param("id") String id);
    List<TransactionOutBoxEntry> findByProcessedAndApplication(@Param("processed") boolean processed, @Param("application") String application, Pageable pageable);
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Modifying
    int deleteByIdAndProcessed(@Param("id") String id, @Param("processed") boolean processed );
    @Modifying
    int deleteByProcessedAndApplication(@Param("processed") boolean processed, @Param("application") String application );
}
