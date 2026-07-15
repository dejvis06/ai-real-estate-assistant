package com.property.property.interfaces.rest;

import com.property.property.application.dto.PropertyResponse;
import com.property.property.application.service.PropertyApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/properties")
public class PropertyController {

    private final PropertyApplicationService propertyApplicationService;

    public PropertyController(PropertyApplicationService propertyApplicationService) {
        this.propertyApplicationService = propertyApplicationService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropertyResponse> getPropertyById(@PathVariable Long id) {
        return propertyApplicationService.getPropertyById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
