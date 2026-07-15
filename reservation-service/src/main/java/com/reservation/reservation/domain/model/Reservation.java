package com.reservation.reservation.domain.model;

import java.time.LocalDateTime;

public class Reservation {

    private final ReservationId id;
    private final String reservationNumber;
    private ReservationStatus status;
    private final Long propertyId;
    private final String customerName;
    private final String customerEmail;
    private final String customerPhone;
    private final ReservationSchedule schedule;
    private final String customerMessage;
    private final String internalNotes;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Reservation(Builder builder) {
        this.id = builder.id;
        this.reservationNumber = builder.reservationNumber;
        this.status = builder.status;
        this.propertyId = builder.propertyId;
        this.customerName = builder.customerName;
        this.customerEmail = builder.customerEmail;
        this.customerPhone = builder.customerPhone;
        this.schedule = builder.schedule;
        this.customerMessage = builder.customerMessage;
        this.internalNotes = builder.internalNotes;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    // -------------------------------------------------------------------------
    // Business rules
    // -------------------------------------------------------------------------

    public ReservationPolicy evaluatePolicy() {
        boolean canCancel = status == ReservationStatus.PENDING || status == ReservationStatus.CONFIRMED;
        boolean canReschedule = status == ReservationStatus.PENDING || status == ReservationStatus.CONFIRMED;

        LocalDateTime cancellationDeadline = schedule != null && schedule.getViewingDateTime() != null
                ? schedule.getViewingDateTime().minusHours(24)
                : null;

        boolean lateCancel = cancellationDeadline != null
                && LocalDateTime.now().isAfter(cancellationDeadline)
                && canCancel;

        CancellationFee fee;
        if (lateCancel) {
            fee = new CancellationFee(true, null, null, "Cancellation requested less than 24 hours before the scheduled viewing.");
        } else {
            fee = new CancellationFee(false, null, null, null);
        }

        java.util.List<String> restrictions = new java.util.ArrayList<>();
        if (status == ReservationStatus.CANCELLED) {
            restrictions.add("This reservation has already been cancelled.");
        }
        if (status == ReservationStatus.COMPLETED) {
            restrictions.add("This reservation has already been completed.");
        }
        if (status == ReservationStatus.NO_SHOW) {
            restrictions.add("This reservation was marked as no-show.");
        }
        if (lateCancel) {
            restrictions.add("Cancellation within 24 hours of the viewing may incur a fee.");
        }

        return new ReservationPolicy(canCancel, canReschedule, cancellationDeadline, fee, restrictions);
    }

    // -------------------------------------------------------------------------
    // State transitions
    // -------------------------------------------------------------------------

    public void cancel() {
        if (status != ReservationStatus.PENDING && status != ReservationStatus.CONFIRMED) {
            throw new IllegalStateException("Only PENDING or CONFIRMED reservations can be cancelled.");
        }
        this.status = ReservationStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    public void confirm() {
        if (status != ReservationStatus.PENDING) {
            throw new IllegalStateException("Only PENDING reservations can be confirmed.");
        }
        this.status = ReservationStatus.CONFIRMED;
        this.updatedAt = LocalDateTime.now();
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public ReservationId getId() { return id; }
    public String getReservationNumber() { return reservationNumber; }
    public ReservationStatus getStatus() { return status; }
    public Long getPropertyId() { return propertyId; }
    public String getCustomerName() { return customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public String getCustomerPhone() { return customerPhone; }
    public ReservationSchedule getSchedule() { return schedule; }
    public String getCustomerMessage() { return customerMessage; }
    public String getInternalNotes() { return internalNotes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private ReservationId id;
        private String reservationNumber;
        private ReservationStatus status;
        private Long propertyId;
        private String customerName;
        private String customerEmail;
        private String customerPhone;
        private ReservationSchedule schedule;
        private String customerMessage;
        private String internalNotes;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(ReservationId id) { this.id = id; return this; }
        public Builder reservationNumber(String reservationNumber) { this.reservationNumber = reservationNumber; return this; }
        public Builder status(ReservationStatus status) { this.status = status; return this; }
        public Builder propertyId(Long propertyId) { this.propertyId = propertyId; return this; }
        public Builder customerName(String customerName) { this.customerName = customerName; return this; }
        public Builder customerEmail(String customerEmail) { this.customerEmail = customerEmail; return this; }
        public Builder customerPhone(String customerPhone) { this.customerPhone = customerPhone; return this; }
        public Builder schedule(ReservationSchedule schedule) { this.schedule = schedule; return this; }
        public Builder customerMessage(String customerMessage) { this.customerMessage = customerMessage; return this; }
        public Builder internalNotes(String internalNotes) { this.internalNotes = internalNotes; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public Reservation build() { return new Reservation(this); }
    }
}
