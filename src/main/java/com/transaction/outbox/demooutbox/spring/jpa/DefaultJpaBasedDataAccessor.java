package com.transaction.outbox.demooutbox.spring.jpa;

import com.transaction.outbox.demooutbox.configuration.ApplicationConfiguration;
import com.transaction.outbox.demooutbox.core.dao.DataAccessor;
import com.transaction.outbox.demooutbox.core.domain.OffsetBasedPageRequest;
import com.transaction.outbox.demooutbox.core.domain.TransactionOutBoxEntry;
import com.transaction.outbox.demooutbox.spring.repository.TransactionOutboxEntryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Slf4j
public class DefaultJpaBasedDataAccessor implements DataAccessor {
    private final TransactionOutboxEntryRepository transactionOutboxEntryRepository;
    private final ApplicationConfiguration applicationConfiguration;

    public DefaultJpaBasedDataAccessor(@NonNull TransactionOutboxEntryRepository transactionOutboxEntryRepository,
                                       @NonNull ApplicationConfiguration applicationConfiguration) {
        this.transactionOutboxEntryRepository = transactionOutboxEntryRepository;
        this.applicationConfiguration = applicationConfiguration;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public TransactionOutBoxEntry save(TransactionOutBoxEntry entry) {
        return transactionOutboxEntryRepository.save(entry);
    }

    @Override
    public int deleteProcessedEntry(String id) {
        return transactionOutboxEntryRepository.deleteByIdAndProcessed(id, true);
    }

    @Override
    public Optional<TransactionOutBoxEntry> findByIdWithLock(String id) {
        return transactionOutboxEntryRepository.findById(id);
    }

    @Override
    public void fetchUnprocessedInBatchAndCallback(int batchSize, Consumer<List<TransactionOutBoxEntry>> consumer) {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("batchSize should be greater than 0");
        }

        long startTime = System.currentTimeMillis();
        int currentOffset = 0;
        long totalRecords = transactionOutboxEntryRepository.count();
        log.debug("Total records found:{}", totalRecords);
        if (totalRecords <= 0) {
            return;
        }
        do {
            log.info("Polling, batchSize: {}, currentOffset: {}", batchSize, currentOffset);
            Pageable pagable = new OffsetBasedPageRequest(currentOffset, batchSize);
            List<TransactionOutBoxEntry> page = transactionOutboxEntryRepository.findByProcessedAndApplication(false,
                    applicationConfiguration.getTranOutboxClient(), pagable);
            if (page.isEmpty()) {
                log.info("nothing to fetch more bye");
                consumer.accept(page);
                currentOffset += page.size();
            } else {
                log.debug("Fetched elements : {}", page);
                break;
            }
        } while (currentOffset < totalRecords);
        long endTime = System.currentTimeMillis();
        log.info("Polling ended in ms: {}", (endTime - startTime));
    }

    @Override
    public void deleteProcessedEntryByApplication() {
        int deletedProcessedEntries = transactionOutboxEntryRepository.deleteByProcessedAndApplication(true, applicationConfiguration.getTranOutboxClient());
        log.info("Found {} processed entries.Submitted for clean-up", deletedProcessedEntries);
    }

    @Override
    public void deleteEntryById(String id) {
        transactionOutboxEntryRepository.deleteById(id);
    }
}
