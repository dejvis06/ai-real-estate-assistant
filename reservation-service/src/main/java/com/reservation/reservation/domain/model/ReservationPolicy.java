package com.reservation.reservation.domain.model;

import java.time.LocalDateTime;
import java.util.List;

public class ReservationPolicy {

    private final boolean canCancel;
    private final boolean canReschedule;
    private final LocalDateTime cancellationDeadline;
    private final CancellationFee cancellationFee;
    private final List<String> restrictions;

    public ReservationPolicy(boolean canCancel, boolean canReschedule,
                             LocalDateTime cancellationDeadline, CancellationFee cancellationFee,
                             List<String> restrictions) {
        this.canCancel = canCancel;
        this.canReschedule = canReschedule;
        this.cancellationDeadline = cancellationDeadline;
        this.cancellationFee = cancellationFee;
        this.restrictions = restrictions != null ? List.copyOf(restrictions) : List.of();
    }

    public boolean isCanCancel() { return canCancel; }
    public boolean isCanReschedule() { return canReschedule; }
    public LocalDateTime getCancellationDeadline() { return cancellationDeadline; }
    public CancellationFee getCancellationFee() { return cancellationFee; }
    public List<String> getRestrictions() { return restrictions; }
}
