package com.reservation.reservation.domain.model;

import java.util.Objects;

public record ReservationId(Long value) {

    public ReservationId {
        Objects.requireNonNull(value, "ReservationId value must not be null");
    }
}
