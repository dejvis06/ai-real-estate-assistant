package com.property.property.domain.model;

import java.util.Objects;

public record PropertyId(Long value) {

    public PropertyId {
        Objects.requireNonNull(value, "PropertyId value must not be null");
    }
}
