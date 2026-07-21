package com.ai.assistant.real.estate.faq.infrastructure.vectorstore;

import com.ai.assistant.real.estate.faq.application.port.FaqVectorStorePort;
import com.ai.assistant.real.estate.faq.domain.model.Faq;
import com.ai.assistant.real.estate.faq.domain.model.FaqId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class FaqPGVectorStoreAdapter implements FaqVectorStorePort {

    private static final Logger log = LoggerFactory.getLogger(FaqPGVectorStoreAdapter.class);

    private static final String FAQ_UUID_NAMESPACE = "faq-namespace:";

    private final VectorStore vectorStore;

    public FaqPGVectorStoreAdapter(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public void add(Faq faq) {
        if (!faq.isActive()) {
            return;
        }
        Document document = buildDocument(faq);
        vectorStore.add(List.of(document));
        log.info("FAQ {} added to vector store", faq.getId().value());
    }

    @Override
    public void update(Faq faq) {
        vectorStore.delete(List.of(documentId(faq.getId())));
        if (faq.isActive()) {
            vectorStore.add(List.of(buildDocument(faq)));
            log.info("FAQ {} updated in vector store", faq.getId().value());
        } else {
            log.info("FAQ {} removed from vector store (inactive)", faq.getId().value());
        }
    }

    @Override
    public void delete(FaqId faqId) {
        vectorStore.delete(List.of(documentId(faqId)));
        log.info("FAQ {} deleted from vector store", faqId.value());
    }

    private Document buildDocument(Faq faq) {
        String content = String.format(
                "Question: %s%nCategory: %s%nAnswer: %s%nKeywords: %s",
                faq.getQuestion(),
                faq.getCategory().name(),
                faq.getAnswer(),
                String.join(", ", faq.getKeywords())
        );
        Map<String, Object> metadata = Map.of(
                "faqId", faq.getId().value(),
                "category", faq.getCategory().name()
        );
        return new Document(documentId(faq.getId()), content, metadata);
    }

    /**
     * Derives a stable, deterministic UUID from the FAQ numeric ID.
     *
     * <p>The {@code vector_store} table declares its {@code id} column as {@code uuid},
     * so every document ID passed to Spring AI's PgVectorStore must be a valid UUID.
     * Using {@link UUID#nameUUIDFromBytes} (UUID v3 / MD5) guarantees that the same
     * FAQ ID always produces the same UUID, which is required for idempotent
     * add/update/delete operations.
     */
    private String documentId(FaqId faqId) {
        String name = FAQ_UUID_NAMESPACE + faqId.value();
        return UUID.nameUUIDFromBytes(name.getBytes()).toString();
    }
}
