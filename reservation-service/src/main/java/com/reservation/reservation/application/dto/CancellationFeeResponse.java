package com.reservation.reservation.application.dto;

import com.reservation.reservation.domain.model.CancellationFee;

import java.math.BigDecimal;

public record CancellationFeeResponse(
        boolean applicable,
        BigDecimal amount,
        String currency,
        String reason
) {

    public static CancellationFeeResponse from(CancellationFee fee) {
        if (fee == null) return new CancellationFeeResponse(false, null, null, null);
        return new CancellationFeeResponse(
                fee.isApplicable(),
                fee.getAmount(),
                fee.getCurrency(),
                fee.getReason()
        );
    }
}
