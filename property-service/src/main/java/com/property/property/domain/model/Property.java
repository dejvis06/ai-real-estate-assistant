package com.property.property.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class Property {

    private final PropertyId id;
    private final String referenceCode;
    private final String title;
    private final String description;
    private final PropertyType propertyType;
    private final ListingType listingType;
    private final PropertyStatus status;
    private final BigDecimal price;
    private final String currency;
    private final Integer bedrooms;
    private final Integer bathrooms;
    private final BigDecimal area;
    private final Integer floor;
    private final Integer totalFloors;
    private final Integer yearBuilt;
    private final String address;
    private final String city;
    private final String country;
    private final BigDecimal latitude;
    private final BigDecimal longitude;
    private final Long agentId;
    private final List<PropertyImage> images;
    private final List<PropertyAmenity> amenities;
    private final List<PropertyNearbyPlace> nearbyPlaces;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private Property(Builder builder) {
        this.id = builder.id;
        this.referenceCode = builder.referenceCode;
        this.title = builder.title;
        this.description = builder.description;
        this.propertyType = builder.propertyType;
        this.listingType = builder.listingType;
        this.status = builder.status;
        this.price = builder.price;
        this.currency = builder.currency;
        this.bedrooms = builder.bedrooms;
        this.bathrooms = builder.bathrooms;
        this.area = builder.area;
        this.floor = builder.floor;
        this.totalFloors = builder.totalFloors;
        this.yearBuilt = builder.yearBuilt;
        this.address = builder.address;
        this.city = builder.city;
        this.country = builder.country;
        this.latitude = builder.latitude;
        this.longitude = builder.longitude;
        this.agentId = builder.agentId;
        this.images = builder.images != null ? Collections.unmodifiableList(builder.images) : List.of();
        this.amenities = builder.amenities != null ? Collections.unmodifiableList(builder.amenities) : List.of();
        this.nearbyPlaces = builder.nearbyPlaces != null ? Collections.unmodifiableList(builder.nearbyPlaces) : List.of();
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    public PropertyId getId() { return id; }
    public String getReferenceCode() { return referenceCode; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public PropertyType getPropertyType() { return propertyType; }
    public ListingType getListingType() { return listingType; }
    public PropertyStatus getStatus() { return status; }
    public BigDecimal getPrice() { return price; }
    public String getCurrency() { return currency; }
    public Integer getBedrooms() { return bedrooms; }
    public Integer getBathrooms() { return bathrooms; }
    public BigDecimal getArea() { return area; }
    public Integer getFloor() { return floor; }
    public Integer getTotalFloors() { return totalFloors; }
    public Integer getYearBuilt() { return yearBuilt; }
    public String getAddress() { return address; }
    public String getCity() { return city; }
    public String getCountry() { return country; }
    public BigDecimal getLatitude() { return latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public Long getAgentId() { return agentId; }
    public List<PropertyImage> getImages() { return images; }
    public List<PropertyAmenity> getAmenities() { return amenities; }
    public List<PropertyNearbyPlace> getNearbyPlaces() { return nearbyPlaces; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private PropertyId id;
        private String referenceCode;
        private String title;
        private String description;
        private PropertyType propertyType;
        private ListingType listingType;
        private PropertyStatus status;
        private BigDecimal price;
        private String currency;
        private Integer bedrooms;
        private Integer bathrooms;
        private BigDecimal area;
        private Integer floor;
        private Integer totalFloors;
        private Integer yearBuilt;
        private String address;
        private String city;
        private String country;
        private BigDecimal latitude;
        private BigDecimal longitude;
        private Long agentId;
        private List<PropertyImage> images;
        private List<PropertyAmenity> amenities;
        private List<PropertyNearbyPlace> nearbyPlaces;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(PropertyId id) { this.id = id; return this; }
        public Builder referenceCode(String referenceCode) { this.referenceCode = referenceCode; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder propertyType(PropertyType propertyType) { this.propertyType = propertyType; return this; }
        public Builder listingType(ListingType listingType) { this.listingType = listingType; return this; }
        public Builder status(PropertyStatus status) { this.status = status; return this; }
        public Builder price(BigDecimal price) { this.price = price; return this; }
        public Builder currency(String currency) { this.currency = currency; return this; }
        public Builder bedrooms(Integer bedrooms) { this.bedrooms = bedrooms; return this; }
        public Builder bathrooms(Integer bathrooms) { this.bathrooms = bathrooms; return this; }
        public Builder area(BigDecimal area) { this.area = area; return this; }
        public Builder floor(Integer floor) { this.floor = floor; return this; }
        public Builder totalFloors(Integer totalFloors) { this.totalFloors = totalFloors; return this; }
        public Builder yearBuilt(Integer yearBuilt) { this.yearBuilt = yearBuilt; return this; }
        public Builder address(String address) { this.address = address; return this; }
        public Builder city(String city) { this.city = city; return this; }
        public Builder country(String country) { this.country = country; return this; }
        public Builder latitude(BigDecimal latitude) { this.latitude = latitude; return this; }
        public Builder longitude(BigDecimal longitude) { this.longitude = longitude; return this; }
        public Builder agentId(Long agentId) { this.agentId = agentId; return this; }
        public Builder images(List<PropertyImage> images) { this.images = images; return this; }
        public Builder amenities(List<PropertyAmenity> amenities) { this.amenities = amenities; return this; }
        public Builder nearbyPlaces(List<PropertyNearbyPlace> nearbyPlaces) { this.nearbyPlaces = nearbyPlaces; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public Property build() { return new Property(this); }
    }
}
