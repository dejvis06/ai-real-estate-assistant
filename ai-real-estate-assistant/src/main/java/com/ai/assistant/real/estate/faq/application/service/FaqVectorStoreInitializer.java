package com.ai.assistant.real.estate.faq.application.service;

import com.ai.assistant.real.estate.faq.application.port.FaqVectorStorePort;
import com.ai.assistant.real.estate.faq.domain.model.Faq;
import com.ai.assistant.real.estate.faq.domain.repository.FaqRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Bootstraps the vector store with all active FAQs from the database on startup.
 *
 * <p>Liquibase seeds the {@code faq} table, but the vector store is empty until
 * an explicit sync is performed. This component listens for
 * {@link ApplicationReadyEvent} — fired after Liquibase, JPA, and the vector
 * store bean are all fully initialised — and upserts every active FAQ.
 *
 * <p>The DB read and the vector store writes intentionally run in separate
 * transactions: the read-only transaction is committed before any write to the
 * vector store begins, so PostgreSQL never sees a DELETE/INSERT inside a
 * read-only transaction.
 */
@Component
public class FaqVectorStoreInitializer {

    private static final Logger log = LoggerFactory.getLogger(FaqVectorStoreInitializer.class);

    private final FaqRepository faqRepository;
    private final FaqVectorStorePort faqVectorStorePort;

    public FaqVectorStoreInitializer(FaqRepository faqRepository,
                                     FaqVectorStorePort faqVectorStorePort) {
        this.faqRepository = faqRepository;
        this.faqVectorStorePort = faqVectorStorePort;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void syncFaqsToVectorStore() {
        List<Faq> allFaqs = loadAllFaqs();

        log.info("FAQ vector store sync started — {} FAQ(s) found in database", allFaqs.size());

        int synced = 0;
        for (Faq faq : allFaqs) {
            try {
                faqVectorStorePort.update(faq);
                synced++;
            } catch (Exception e) {
                log.error("Failed to sync FAQ id={} to vector store: {}", faq.getId().value(), e.getMessage(), e);
            }
        }

        log.info("FAQ vector store sync completed — {}/{} FAQ(s) synced", synced, allFaqs.size());
    }

    @Transactional(readOnly = true)
    public List<Faq> loadAllFaqs() {
        return faqRepository.findAll();
    }
}
