package com.ai.assistant.real.estate.faq.infrastructure.persistence.mapper;

import com.ai.assistant.real.estate.faq.domain.model.Faq;
import com.ai.assistant.real.estate.faq.domain.model.FaqCategory;
import com.ai.assistant.real.estate.faq.domain.model.FaqId;
import com.ai.assistant.real.estate.faq.infrastructure.persistence.entity.FaqJpaEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FaqMapperTest {

    private FaqMapper mapper;
    private final LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        mapper = new FaqMapper();
    }

    // ── toDomain ────────────────────────────────────────────────────────────

    @Test
    void toDomain_shouldMapAllScalarFields() {
        FaqJpaEntity entity = buildEntity(1L);

        Faq faq = mapper.toDomain(entity);

        assertThat(faq.getId().value()).isEqualTo(1L);
        assertThat(faq.getQuestion()).isEqualTo("Question?");
        assertThat(faq.getAnswer()).isEqualTo("Answer.");
        assertThat(faq.getCategory()).isEqualTo(FaqCategory.BUYING);
        assertThat(faq.isActive()).isTrue();
        assertThat(faq.getCreatedAt()).isEqualTo(now);
        assertThat(faq.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void toDomain_shouldMapKeywords() {
        FaqJpaEntity entity = buildEntity(1L);
        entity.setKeywords(List.of("viewing", "appointment"));

        Faq faq = mapper.toDomain(entity);

        assertThat(faq.getKeywords()).containsExactly("viewing", "appointment");
    }

    @Test
    void toDomain_withEmptyKeywords_shouldProduceEmptyList() {
        FaqJpaEntity entity = buildEntity(1L);
        entity.setKeywords(List.of());

        Faq faq = mapper.toDomain(entity);

        assertThat(faq.getKeywords()).isEmpty();
    }

    // ── toEntity ────────────────────────────────────────────────────────────

    @Test
    void toEntity_shouldMapAllScalarFields() {
        Faq faq = Faq.reconstitute(new FaqId(5L), "Q?", "A.", FaqCategory.SELLING,
                List.of("sell"), true, now, now);

        FaqJpaEntity entity = mapper.toEntity(faq);

        assertThat(entity.getId()).isEqualTo(5L);
        assertThat(entity.getQuestion()).isEqualTo("Q?");
        assertThat(entity.getAnswer()).isEqualTo("A.");
        assertThat(entity.getCategory()).isEqualTo(FaqCategory.SELLING);
        assertThat(entity.isActive()).isTrue();
        assertThat(entity.getCreatedAt()).isEqualTo(now);
        assertThat(entity.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void toEntity_shouldMapKeywords() {
        Faq faq = Faq.reconstitute(new FaqId(1L), "Q?", "A.", FaqCategory.BUYING,
                List.of("kw1", "kw2"), true, now, now);

        FaqJpaEntity entity = mapper.toEntity(faq);

        assertThat(entity.getKeywords()).containsExactly("kw1", "kw2");
    }

    @Test
    void toEntity_withNullId_shouldLeaveIdNull() {
        Faq faq = Faq.create("Q?", "A.", FaqCategory.BUYING, List.of());

        FaqJpaEntity entity = mapper.toEntity(faq);

        assertThat(entity.getId()).isNull();
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private FaqJpaEntity buildEntity(Long id) {
        FaqJpaEntity entity = new FaqJpaEntity();
        entity.setId(id);
        entity.setQuestion("Question?");
        entity.setAnswer("Answer.");
        entity.setCategory(FaqCategory.BUYING);
        entity.setKeywords(List.of());
        entity.setActive(true);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        return entity;
    }
}
