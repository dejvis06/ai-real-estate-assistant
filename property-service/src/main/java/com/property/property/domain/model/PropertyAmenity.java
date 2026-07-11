package com.property.property.domain.model;

import java.time.LocalDateTime;

public class PropertyAmenity {

    private final Long id;
    private final String name;
    private final LocalDateTime createdAt;

    public PropertyAmenity(Long id, String name, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
