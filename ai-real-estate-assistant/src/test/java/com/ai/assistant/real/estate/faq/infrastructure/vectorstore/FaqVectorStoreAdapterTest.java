package com.ai.assistant.real.estate.faq.infrastructure.vectorstore;

import com.ai.assistant.real.estate.faq.domain.model.Faq;
import com.ai.assistant.real.estate.faq.domain.model.FaqCategory;
import com.ai.assistant.real.estate.faq.domain.model.FaqId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FaqVectorStoreAdapterTest {

    @Mock
    VectorStore vectorStore;

    FaqVectorStoreAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new FaqVectorStoreAdapter(vectorStore);
    }

    // ── add ──────────────────────────────────────────────────────────────────

    @Test
    void add_activeFaq_shouldAddDocumentToVectorStore() {
        Faq faq = reconstitute(1L, true);

        adapter.add(faq);

        var captor = ArgumentCaptor.forClass(List.class);
        verify(vectorStore).add(captor.capture());

        List<Document> docs = captor.getValue();
        assertThat(docs).hasSize(1);
        Document doc = docs.get(0);
        assertThat(doc.getId()).isEqualTo("faq-1");
        assertThat(doc.getText()).contains("Question: Q?");
        assertThat(doc.getText()).contains("Category: BUYING");
        assertThat(doc.getText()).contains("Answer: A.");
        assertThat(doc.getText()).contains("Keywords: kw1, kw2");
    }

    @Test
    void add_inactiveFaq_shouldNotAddToVectorStore() {
        Faq faq = reconstitute(2L, false);

        adapter.add(faq);

        verify(vectorStore, never()).add(anyList());
    }

    // ── update ───────────────────────────────────────────────────────────────

    @Test
    void update_activeFaq_shouldDeleteThenAdd() {
        Faq faq = reconstitute(3L, true);

        adapter.update(faq);

        verify(vectorStore).delete(List.of("faq-3"));
        verify(vectorStore).add(anyList());
    }

    @Test
    void update_inactiveFaq_shouldOnlyDelete() {
        Faq faq = reconstitute(4L, false);

        adapter.update(faq);

        verify(vectorStore).delete(List.of("faq-4"));
        verify(vectorStore, never()).add(anyList());
    }

    // ── delete ───────────────────────────────────────────────────────────────

    @Test
    void delete_shouldDeleteByDocumentId() {
        adapter.delete(new FaqId(5L));

        verify(vectorStore).delete(List.of("faq-5"));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Faq reconstitute(Long id, boolean active) {
        LocalDateTime now = LocalDateTime.now();
        return Faq.reconstitute(
                new FaqId(id), "Q?", "A.", FaqCategory.BUYING,
                List.of("kw1", "kw2"), active, now, now
        );
    }
}
