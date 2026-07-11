package com.ai.assistant.real.estate.faq.domain.repository;

import com.ai.assistant.real.estate.faq.domain.model.Faq;
import com.ai.assistant.real.estate.faq.domain.model.FaqId;

import java.util.List;
import java.util.Optional;

public interface FaqRepository {

    Faq save(Faq faq);

    Optional<Faq> findById(FaqId id);

    List<Faq> findAll();

    void deleteById(FaqId id);

    boolean existsById(FaqId id);
}
