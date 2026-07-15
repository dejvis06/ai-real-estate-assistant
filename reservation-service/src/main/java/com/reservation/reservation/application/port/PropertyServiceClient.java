package com.reservation.reservation.application.port;

import com.reservation.reservation.application.dto.PropertyResponse;

import java.util.Optional;

public interface PropertyServiceClient {

    Optional<PropertyResponse> findById(Long propertyId);
}
