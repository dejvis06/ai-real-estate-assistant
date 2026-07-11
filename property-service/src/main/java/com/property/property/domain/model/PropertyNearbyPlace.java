package com.property.property.domain.model;

import java.math.BigDecimal;

public class PropertyNearbyPlace {

    private final Long id;
    private final String name;
    private final String type;
    private final BigDecimal distance;
    private final String distanceUnit;
    private final BigDecimal latitude;
    private final BigDecimal longitude;

    public PropertyNearbyPlace(Long id, String name, String type, BigDecimal distance,
                               String distanceUnit, BigDecimal latitude, BigDecimal longitude) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.distance = distance;
        this.distanceUnit = distanceUnit;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public BigDecimal getDistance() { return distance; }
    public String getDistanceUnit() { return distanceUnit; }
    public BigDecimal getLatitude() { return latitude; }
    public BigDecimal getLongitude() { return longitude; }
}
