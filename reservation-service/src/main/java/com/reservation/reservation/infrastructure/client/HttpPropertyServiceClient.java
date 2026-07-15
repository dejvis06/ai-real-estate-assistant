package com.reservation.reservation.infrastructure.client;

import com.reservation.reservation.application.dto.PropertyResponse;
import com.reservation.reservation.application.port.PropertyServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@Component
public class HttpPropertyServiceClient implements PropertyServiceClient {

    private static final Logger log = LoggerFactory.getLogger(HttpPropertyServiceClient.class);

    private final RestClient restClient;

    public HttpPropertyServiceClient(RestClient.Builder restClientBuilder,
                                     @Value("${clients.property-service.url}") String propertyServiceUrl) {
        this.restClient = restClientBuilder
                .baseUrl(propertyServiceUrl)
                .build();
    }

    @Override
    public Optional<PropertyResponse> findById(Long propertyId) {
        try {
            PropertyResponse response = restClient.get()
                    .uri("/api/properties/{id}", propertyId)
                    .retrieve()
                    .body(PropertyResponse.class);
            return Optional.ofNullable(response);
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Property not found in property-service for id={}", propertyId);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Failed to fetch property id={} from property-service: {}", propertyId, e.getMessage());
            return Optional.empty();
        }
    }
}
