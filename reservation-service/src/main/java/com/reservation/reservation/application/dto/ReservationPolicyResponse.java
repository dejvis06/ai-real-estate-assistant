package com.reservation.reservation.application.dto;

import com.reservation.reservation.domain.model.ReservationPolicy;

import java.time.LocalDateTime;
import java.util.List;

public record ReservationPolicyResponse(
        boolean canCancel,
        boolean canReschedule,
        LocalDateTime cancellationDeadline,
        CancellationFeeResponse cancellationFee,
        List<String> restrictions
) {

    public static ReservationPolicyResponse from(ReservationPolicy policy) {
        return new ReservationPolicyResponse(
                policy.isCanCancel(),
                policy.isCanReschedule(),
                policy.getCancellationDeadline(),
                CancellationFeeResponse.from(policy.getCancellationFee()),
                policy.getRestrictions()
        );
    }
}
