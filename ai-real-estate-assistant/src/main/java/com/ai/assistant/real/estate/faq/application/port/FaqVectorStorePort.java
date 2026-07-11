package com.ai.assistant.real.estate.faq.application.port;

import com.ai.assistant.real.estate.faq.domain.model.Faq;
import com.ai.assistant.real.estate.faq.domain.model.FaqId;

public interface FaqVectorStorePort {

    void add(Faq faq);

    void update(Faq faq);

    void delete(FaqId faqId);
}
