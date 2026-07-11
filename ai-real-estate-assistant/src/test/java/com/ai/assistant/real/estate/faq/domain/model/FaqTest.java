package com.ai.assistant.real.estate.faq.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FaqTest {

    @Test
    void create_shouldSetActiveByDefault() {
        Faq faq = Faq.create("Q?", "A.", FaqCategory.BUYING, List.of());
        assertThat(faq.isActive()).isTrue();
    }

    @Test
    void create_shouldSetTimestamps() {
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        Faq faq = Faq.create("Q?", "A.", FaqCategory.BUYING, List.of());
        assertThat(faq.getCreatedAt()).isAfter(before);
        assertThat(faq.getUpdatedAt()).isAfter(before);
    }

    @Test
    void create_shouldTrimQuestionAndAnswer() {
        Faq faq = Faq.create("  Q?  ", "  A.  ", FaqCategory.BUYING, List.of());
        assertThat(faq.getQuestion()).isEqualTo("Q?");
        assertThat(faq.getAnswer()).isEqualTo("A.");
    }

    @Test
    void create_withNullQuestion_shouldThrow() {
        assertThatThrownBy(() -> Faq.create(null, "A.", FaqCategory.BUYING, List.of()))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("question");
    }

    @Test
    void create_withNullAnswer_shouldThrow() {
        assertThatThrownBy(() -> Faq.create("Q?", null, FaqCategory.BUYING, List.of()))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("answer");
    }

    @Test
    void create_withNullCategory_shouldThrow() {
        assertThatThrownBy(() -> Faq.create("Q?", "A.", null, List.of()))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("category");
    }

    @Test
    void create_withNullKeywords_shouldDefaultToEmptyList() {
        Faq faq = Faq.create("Q?", "A.", FaqCategory.BUYING, null);
        assertThat(faq.getKeywords()).isEmpty();
    }

    @Test
    void create_shouldStoreKeywords() {
        Faq faq = Faq.create("Q?", "A.", FaqCategory.BUYING, List.of("kw1", "kw2"));
        assertThat(faq.getKeywords()).containsExactly("kw1", "kw2");
    }

    @Test
    void getKeywords_shouldReturnUnmodifiableList() {
        Faq faq = Faq.create("Q?", "A.", FaqCategory.BUYING, List.of("kw1"));
        assertThatThrownBy(() -> faq.getKeywords().add("kw2"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void update_shouldReplaceAllFields() {
        Faq faq = Faq.create("Old Q?", "Old A.", FaqCategory.BUYING, List.of("old"));
        LocalDateTime beforeUpdate = LocalDateTime.now().minusSeconds(1);

        faq.update("New Q?", "New A.", FaqCategory.SELLING, List.of("new"));

        assertThat(faq.getQuestion()).isEqualTo("New Q?");
        assertThat(faq.getAnswer()).isEqualTo("New A.");
        assertThat(faq.getCategory()).isEqualTo(FaqCategory.SELLING);
        assertThat(faq.getKeywords()).containsExactly("new");
        assertThat(faq.getUpdatedAt()).isAfter(beforeUpdate);
    }

    @Test
    void deactivate_shouldSetActiveFalse() {
        Faq faq = Faq.create("Q?", "A.", FaqCategory.BUYING, List.of());
        faq.deactivate();
        assertThat(faq.isActive()).isFalse();
    }

    @Test
    void activate_shouldSetActiveTrue() {
        Faq faq = Faq.create("Q?", "A.", FaqCategory.BUYING, List.of());
        faq.deactivate();
        faq.activate();
        assertThat(faq.isActive()).isTrue();
    }

    @Test
    void reconstitute_shouldRestoreAllFields() {
        LocalDateTime now = LocalDateTime.now();
        Faq faq = Faq.reconstitute(
                new FaqId(42L), "Q?", "A.", FaqCategory.FINANCING,
                List.of("kw"), false, now, now);

        assertThat(faq.getId().value()).isEqualTo(42L);
        assertThat(faq.getQuestion()).isEqualTo("Q?");
        assertThat(faq.getAnswer()).isEqualTo("A.");
        assertThat(faq.getCategory()).isEqualTo(FaqCategory.FINANCING);
        assertThat(faq.getKeywords()).containsExactly("kw");
        assertThat(faq.isActive()).isFalse();
        assertThat(faq.getCreatedAt()).isEqualTo(now);
        assertThat(faq.getUpdatedAt()).isEqualTo(now);
    }
}
