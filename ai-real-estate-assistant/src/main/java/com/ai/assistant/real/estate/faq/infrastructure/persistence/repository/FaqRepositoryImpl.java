package com.ai.assistant.real.estate.faq.infrastructure.persistence.repository;

import com.ai.assistant.real.estate.faq.domain.model.Faq;
import com.ai.assistant.real.estate.faq.domain.model.FaqId;
import com.ai.assistant.real.estate.faq.domain.repository.FaqRepository;
import com.ai.assistant.real.estate.faq.infrastructure.persistence.mapper.FaqMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class FaqRepositoryImpl implements FaqRepository {

    private final FaqJpaRepository faqJpaRepository;
    private final FaqMapper faqMapper;

    public FaqRepositoryImpl(FaqJpaRepository faqJpaRepository, FaqMapper faqMapper) {
        this.faqJpaRepository = faqJpaRepository;
        this.faqMapper = faqMapper;
    }

    @Override
    public Faq save(Faq faq) {
        var entity = faqMapper.toEntity(faq);
        var saved = faqJpaRepository.save(entity);
        return faqMapper.toDomain(saved);
    }

    @Override
    public Optional<Faq> findById(FaqId id) {
        return faqJpaRepository.findById(id.value())
                .map(faqMapper::toDomain);
    }

    @Override
    public List<Faq> findAll() {
        return faqJpaRepository.findAll().stream()
                .map(faqMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(FaqId id) {
        faqJpaRepository.deleteById(id.value());
    }

    @Override
    public boolean existsById(FaqId id) {
        return faqJpaRepository.existsById(id.value());
    }
}
