package com.ai.assistant.real.estate.faq.interfaces.rest;

import com.ai.assistant.real.estate.faq.application.dto.FaqRequest;
import com.ai.assistant.real.estate.faq.application.dto.FaqResponse;
import com.ai.assistant.real.estate.faq.application.service.FaqApplicationService;
import com.ai.assistant.real.estate.faq.application.service.FaqNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/faqs")
public class FaqController {

    private final FaqApplicationService faqApplicationService;

    public FaqController(FaqApplicationService faqApplicationService) {
        this.faqApplicationService = faqApplicationService;
    }

    @GetMapping
    public ResponseEntity<List<FaqResponse>> getAllFaqs() {
        return ResponseEntity.ok(faqApplicationService.getAllFaqs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FaqResponse> getFaq(@PathVariable Long id) {
        return ResponseEntity.ok(faqApplicationService.getFaq(id));
    }

    @PostMapping
    public ResponseEntity<FaqResponse> createFaq(@RequestBody FaqRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(faqApplicationService.createFaq(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FaqResponse> updateFaq(@PathVariable Long id, @RequestBody FaqRequest request) {
        return ResponseEntity.ok(faqApplicationService.updateFaq(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFaq(@PathVariable Long id) {
        faqApplicationService.deleteFaq(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(FaqNotFoundException.class)
    public ResponseEntity<String> handleFaqNotFound(FaqNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}
