package com.ai.assistant.real.estate.faq.application.service;

import com.ai.assistant.real.estate.faq.application.dto.FaqRequest;
import com.ai.assistant.real.estate.faq.application.dto.FaqResponse;
import com.ai.assistant.real.estate.faq.application.port.FaqVectorStorePort;
import com.ai.assistant.real.estate.faq.domain.model.Faq;
import com.ai.assistant.real.estate.faq.domain.model.FaqId;
import com.ai.assistant.real.estate.faq.domain.repository.FaqRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class FaqApplicationService {

    private final FaqRepository faqRepository;
    private final FaqVectorStorePort faqVectorStorePort;

    public FaqApplicationService(FaqRepository faqRepository, FaqVectorStorePort faqVectorStorePort) {
        this.faqRepository = faqRepository;
        this.faqVectorStorePort = faqVectorStorePort;
    }

    public FaqResponse createFaq(FaqRequest request) {
        Faq faq = Faq.create(request.question(), request.answer(), request.category(),
                request.keywords());
        Faq saved = faqRepository.save(faq);
        faqVectorStorePort.add(saved);
        return FaqResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public FaqResponse getFaq(Long id) {
        return faqRepository.findById(new FaqId(id))
                .map(FaqResponse::from)
                .orElseThrow(() -> new FaqNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<FaqResponse> getAllFaqs() {
        return faqRepository.findAll().stream()
                .map(FaqResponse::from)
                .toList();
    }

    public FaqResponse updateFaq(Long id, FaqRequest request) {
        Faq faq = faqRepository.findById(new FaqId(id))
                .orElseThrow(() -> new FaqNotFoundException(id));
        faq.update(request.question(), request.answer(), request.category(), request.keywords());
        if (request.active() != null) {
            if (request.active()) faq.activate(); else faq.deactivate();
        }
        Faq updated = faqRepository.save(faq);
        faqVectorStorePort.update(updated);
        return FaqResponse.from(updated);
    }

    public void deleteFaq(Long id) {
        if (!faqRepository.existsById(new FaqId(id))) {
            throw new FaqNotFoundException(id);
        }
        faqRepository.deleteById(new FaqId(id));
        faqVectorStorePort.delete(new FaqId(id));
    }
}
