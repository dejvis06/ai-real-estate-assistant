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

@Component
public class FaqVectorStoreAdapter implements FaqVectorStorePort {

    private static final Logger log = LoggerFactory.getLogger(FaqVectorStoreAdapter.class);

    private final VectorStore vectorStore;

    public FaqVectorStoreAdapter(VectorStore vectorStore) {
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

    private String documentId(FaqId faqId) {
        return "faq-" + faqId.value();
    }
}
