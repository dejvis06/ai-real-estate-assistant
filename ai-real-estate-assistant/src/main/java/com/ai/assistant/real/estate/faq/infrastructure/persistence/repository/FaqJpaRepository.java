package com.ai.assistant.real.estate.faq.infrastructure.persistence.repository;

import com.ai.assistant.real.estate.faq.infrastructure.persistence.entity.FaqJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FaqJpaRepository extends JpaRepository<FaqJpaEntity, Long> {}
