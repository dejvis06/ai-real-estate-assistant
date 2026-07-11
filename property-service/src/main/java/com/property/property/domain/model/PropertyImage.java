package com.property.property.domain.model;

import java.time.LocalDateTime;

public class PropertyImage {

    private final Long id;
    private final String url;
    private final int displayOrder;
    private final boolean primary;
    private final LocalDateTime createdAt;

    public PropertyImage(Long id, String url, int displayOrder, boolean primary, LocalDateTime createdAt) {
        this.id = id;
        this.url = url;
        this.displayOrder = displayOrder;
        this.primary = primary;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getUrl() { return url; }
    public int getDisplayOrder() { return displayOrder; }
    public boolean isPrimary() { return primary; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
