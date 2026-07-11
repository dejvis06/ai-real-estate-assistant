package com.ai.assistant.real.estate.faq.application.service;

import com.ai.assistant.real.estate.faq.application.dto.FaqRequest;
import com.ai.assistant.real.estate.faq.application.dto.FaqResponse;
import com.ai.assistant.real.estate.faq.application.port.FaqVectorStorePort;
import com.ai.assistant.real.estate.faq.domain.model.Faq;
import com.ai.assistant.real.estate.faq.domain.model.FaqCategory;
import com.ai.assistant.real.estate.faq.domain.model.FaqId;
import com.ai.assistant.real.estate.faq.domain.repository.FaqRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FaqApplicationServiceTest {

    @Mock
    FaqRepository faqRepository;

    @Mock
    FaqVectorStorePort faqVectorStorePort;

    @InjectMocks
    FaqApplicationService service;

    // ── createFaq ────────────────────────────────────────────────────────────

    @Test
    void createFaq_shouldSaveAndAddToVectorStore() {
        var request = new FaqRequest("Q?", "A.", FaqCategory.BUYING, List.of("kw"), null);
        LocalDateTime now = LocalDateTime.now();
        Faq savedFaq = Faq.reconstitute(new FaqId(1L), "Q?", "A.", FaqCategory.BUYING,
                List.of("kw"), true, now, now);
        when(faqRepository.save(any())).thenReturn(savedFaq);

        FaqResponse response = service.createFaq(request);

        verify(faqRepository).save(any(Faq.class));
        verify(faqVectorStorePort).add(savedFaq);
        assertThat(response.question()).isEqualTo("Q?");
        assertThat(response.answer()).isEqualTo("A.");
        assertThat(response.category()).isEqualTo("BUYING");
    }

    @Test
    void createFaq_shouldCreateDomainObjectWithCorrectFields() {
        var request = new FaqRequest("Q?", "A.", FaqCategory.SELLING, List.of("kw1"), null);
        LocalDateTime now = LocalDateTime.now();
        when(faqRepository.save(any())).thenAnswer(inv -> {
            Faq faq = inv.getArgument(0);
            // simulate what the repo does: assign a generated ID before returning
            return Faq.reconstitute(new FaqId(1L), faq.getQuestion(), faq.getAnswer(),
                    faq.getCategory(), faq.getKeywords(), faq.isActive(), now, now);
        });

        service.createFaq(request);

        var captor = ArgumentCaptor.forClass(Faq.class);
        verify(faqRepository).save(captor.capture());
        Faq captured = captor.getValue();
        assertThat(captured.getQuestion()).isEqualTo("Q?");
        assertThat(captured.getCategory()).isEqualTo(FaqCategory.SELLING);
        assertThat(captured.isActive()).isTrue();
    }

    // ── getFaq ───────────────────────────────────────────────────────────────

    @Test
    void getFaq_shouldReturnFaqWhenFound() {
        LocalDateTime now = LocalDateTime.now();
        Faq faq = Faq.reconstitute(new FaqId(1L), "Q?", "A.", FaqCategory.BUYING, List.of(), true, now, now);
        when(faqRepository.findById(new FaqId(1L))).thenReturn(Optional.of(faq));

        FaqResponse response = service.getFaq(1L);

        assertThat(response.question()).isEqualTo("Q?");
    }

    @Test
    void getFaq_whenNotFound_shouldThrowFaqNotFoundException() {
        when(faqRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getFaq(99L))
                .isInstanceOf(FaqNotFoundException.class);
    }

    // ── getAllFaqs ────────────────────────────────────────────────────────────

    @Test
    void getAllFaqs_shouldReturnAllFaqs() {
        LocalDateTime now = LocalDateTime.now();
        var faq1 = Faq.reconstitute(new FaqId(1L), "Q1?", "A1.", FaqCategory.BUYING, List.of(), true, now, now);
        var faq2 = Faq.reconstitute(new FaqId(2L), "Q2?", "A2.", FaqCategory.SELLING, List.of(), true, now, now);
        when(faqRepository.findAll()).thenReturn(List.of(faq1, faq2));

        List<FaqResponse> result = service.getAllFaqs();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(FaqResponse::question)
                .containsExactly("Q1?", "Q2?");
    }

    @Test
    void getAllFaqs_whenEmpty_shouldReturnEmptyList() {
        when(faqRepository.findAll()).thenReturn(List.of());

        assertThat(service.getAllFaqs()).isEmpty();
    }

    // ── updateFaq ────────────────────────────────────────────────────────────

    @Test
    void updateFaq_shouldUpdateFieldsAndSyncVectorStore() {
        LocalDateTime now = LocalDateTime.now();
        Faq faq = Faq.reconstitute(new FaqId(1L), "Old Q?", "Old A.", FaqCategory.BUYING, List.of(), true, now, now);
        when(faqRepository.findById(new FaqId(1L))).thenReturn(Optional.of(faq));
        when(faqRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var request = new FaqRequest("New Q?", "New A.", FaqCategory.SELLING, List.of("kw"), null);
        FaqResponse response = service.updateFaq(1L, request);

        assertThat(response.question()).isEqualTo("New Q?");
        assertThat(response.category()).isEqualTo("SELLING");
        verify(faqVectorStorePort).update(any(Faq.class));
    }

    @Test
    void updateFaq_withActiveFlag_shouldDeactivateFaq() {
        LocalDateTime now = LocalDateTime.now();
        Faq faq = Faq.reconstitute(new FaqId(1L), "Q?", "A.", FaqCategory.BUYING, List.of(), true, now, now);
        when(faqRepository.findById(any())).thenReturn(Optional.of(faq));
        when(faqRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var request = new FaqRequest("Q?", "A.", FaqCategory.BUYING, List.of(), false);
        FaqResponse response = service.updateFaq(1L, request);

        assertThat(response.active()).isFalse();
    }

    @Test
    void updateFaq_whenNotFound_shouldThrowFaqNotFoundException() {
        when(faqRepository.findById(any())).thenReturn(Optional.empty());
        var request = new FaqRequest("Q?", "A.", FaqCategory.BUYING, List.of(), null);

        assertThatThrownBy(() -> service.updateFaq(99L, request))
                .isInstanceOf(FaqNotFoundException.class);

        verify(faqVectorStorePort, never()).update(any());
    }

    // ── deleteFaq ────────────────────────────────────────────────────────────

    @Test
    void deleteFaq_shouldDeleteFromRepositoryAndVectorStore() {
        when(faqRepository.existsById(new FaqId(1L))).thenReturn(true);

        service.deleteFaq(1L);

        verify(faqRepository).deleteById(new FaqId(1L));
        verify(faqVectorStorePort).delete(new FaqId(1L));
    }

    @Test
    void deleteFaq_whenNotFound_shouldThrowFaqNotFoundException() {
        when(faqRepository.existsById(any())).thenReturn(false);

        assertThatThrownBy(() -> service.deleteFaq(99L))
                .isInstanceOf(FaqNotFoundException.class);

        verify(faqRepository, never()).deleteById(any());
        verify(faqVectorStorePort, never()).delete(any());
    }
}
