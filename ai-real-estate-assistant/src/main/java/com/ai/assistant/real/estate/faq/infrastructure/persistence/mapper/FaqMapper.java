package com.ai.assistant.real.estate.faq.infrastructure.persistence.mapper;

import com.ai.assistant.real.estate.faq.domain.model.Faq;
import com.ai.assistant.real.estate.faq.domain.model.FaqId;
import com.ai.assistant.real.estate.faq.infrastructure.persistence.entity.FaqJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class FaqMapper {

    public Faq toDomain(FaqJpaEntity entity) {
        return Faq.reconstitute(
                new FaqId(entity.getId()),
                entity.getQuestion(),
                entity.getAnswer(),
                entity.getCategory(),
                entity.getKeywords(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public FaqJpaEntity toEntity(Faq faq) {
        FaqJpaEntity entity = new FaqJpaEntity();
        if (faq.getId() != null) {
            entity.setId(faq.getId().value());
        }
        entity.setQuestion(faq.getQuestion());
        entity.setAnswer(faq.getAnswer());
        entity.setCategory(faq.getCategory());
        entity.setKeywords(faq.getKeywords());
        entity.setActive(faq.isActive());
        entity.setCreatedAt(faq.getCreatedAt());
        entity.setUpdatedAt(faq.getUpdatedAt());
        return entity;
    }
}
