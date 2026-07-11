package com.property.property.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "property_image")
public class PropertyImageJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private PropertyJpaEntity property;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(nullable = false)
    private int displayOrder;

    @Column(nullable = false)
    private boolean isPrimary;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public PropertyJpaEntity getProperty() { return property; }
    public void setProperty(PropertyJpaEntity property) { this.property = property; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
    public boolean isPrimary() { return isPrimary; }
    public void setPrimary(boolean primary) { isPrimary = primary; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
