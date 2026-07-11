package com.ai.assistant.real.estate.faq.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Faq {

    private FaqId id;
    private String question;
    private String answer;
    private FaqCategory category;
    private List<String> keywords;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Faq() {}

    public static Faq create(String question, String answer, FaqCategory category, List<String> keywords) {
        Objects.requireNonNull(question, "question must not be null");
        Objects.requireNonNull(answer, "answer must not be null");
        Objects.requireNonNull(category, "category must not be null");

        Faq faq = new Faq();
        faq.question = question.trim();
        faq.answer = answer.trim();
        faq.category = category;
        faq.keywords = new ArrayList<>(keywords != null ? keywords : List.of());
        faq.active = true;
        faq.createdAt = LocalDateTime.now();
        faq.updatedAt = LocalDateTime.now();
        return faq;
    }

    public static Faq reconstitute(FaqId id, String question, String answer, FaqCategory category,
                                   List<String> keywords, boolean active,
                                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        Faq faq = new Faq();
        faq.id = id;
        faq.question = question;
        faq.answer = answer;
        faq.category = category;
        faq.keywords = new ArrayList<>(keywords != null ? keywords : List.of());
        faq.active = active;
        faq.createdAt = createdAt;
        faq.updatedAt = updatedAt;
        return faq;
    }

    public void update(String question, String answer, FaqCategory category, List<String> keywords) {
        Objects.requireNonNull(question, "question must not be null");
        Objects.requireNonNull(answer, "answer must not be null");
        Objects.requireNonNull(category, "category must not be null");

        this.question = question.trim();
        this.answer = answer.trim();
        this.category = category;
        this.keywords = new ArrayList<>(keywords != null ? keywords : List.of());
        this.updatedAt = LocalDateTime.now();
    }

    public void activate() {
        this.active = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.active = false;
        this.updatedAt = LocalDateTime.now();
    }

    void assignId(FaqId id) {
        this.id = id;
    }

    public FaqId getId() { return id; }
    public String getQuestion() { return question; }
    public String getAnswer() { return answer; }
    public FaqCategory getCategory() { return category; }
    public List<String> getKeywords() { return Collections.unmodifiableList(keywords); }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
