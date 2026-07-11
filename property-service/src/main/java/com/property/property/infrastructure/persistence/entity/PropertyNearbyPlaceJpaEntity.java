package com.property.property.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "property_nearby_place")
public class PropertyNearbyPlaceJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private PropertyJpaEntity property;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 50)
    private String type;

    @Column(precision = 10, scale = 2)
    private BigDecimal distance;

    @Column(length = 5)
    private String distanceUnit;

    @Column(precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(precision = 11, scale = 8)
    private BigDecimal longitude;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public PropertyJpaEntity getProperty() { return property; }
    public void setProperty(PropertyJpaEntity property) { this.property = property; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public BigDecimal getDistance() { return distance; }
    public void setDistance(BigDecimal distance) { this.distance = distance; }
    public String getDistanceUnit() { return distanceUnit; }
    public void setDistanceUnit(String distanceUnit) { this.distanceUnit = distanceUnit; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
}
