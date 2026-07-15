package com.reservation.reservation.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationTest {

    @Test
    void evaluatePolicy_pendingWithFutureViewing_canCancelAndRescheduleNoFee() {
        Reservation reservation = build(ReservationStatus.PENDING, LocalDateTime.now().plusDays(5));

        ReservationPolicy policy = reservation.evaluatePolicy();

        assertThat(policy.isCanCancel()).isTrue();
        assertThat(policy.isCanReschedule()).isTrue();
        assertThat(policy.getCancellationFee().isApplicable()).isFalse();
        assertThat(policy.getRestrictions()).isEmpty();
    }

    @Test
    void evaluatePolicy_confirmedWithFutureViewing_canCancelAndReschedule() {
        Reservation reservation = build(ReservationStatus.CONFIRMED, LocalDateTime.now().plusDays(3));

        ReservationPolicy policy = reservation.evaluatePolicy();

        assertThat(policy.isCanCancel()).isTrue();
        assertThat(policy.isCanReschedule()).isTrue();
    }

    @Test
    void evaluatePolicy_confirmedWithin24Hours_feeApplies() {
        Reservation reservation = build(ReservationStatus.CONFIRMED, LocalDateTime.now().plusHours(6));

        ReservationPolicy policy = reservation.evaluatePolicy();

        assertThat(policy.getCancellationFee().isApplicable()).isTrue();
        assertThat(policy.getRestrictions()).anyMatch(r -> r.contains("24 hours"));
    }

    @Test
    void evaluatePolicy_cancelled_cannotModify() {
        Reservation reservation = build(ReservationStatus.CANCELLED, LocalDateTime.now().plusDays(2));

        ReservationPolicy policy = reservation.evaluatePolicy();

        assertThat(policy.isCanCancel()).isFalse();
        assertThat(policy.isCanReschedule()).isFalse();
        assertThat(policy.getRestrictions()).contains("This reservation has already been cancelled.");
    }

    @Test
    void evaluatePolicy_completed_cannotModify() {
        Reservation reservation = build(ReservationStatus.COMPLETED, LocalDateTime.now().minusDays(1));

        ReservationPolicy policy = reservation.evaluatePolicy();

        assertThat(policy.isCanCancel()).isFalse();
        assertThat(policy.isCanReschedule()).isFalse();
        assertThat(policy.getRestrictions()).contains("This reservation has already been completed.");
    }

    @Test
    void cancel_pendingReservation_becomesCANCELLED() {
        Reservation reservation = build(ReservationStatus.PENDING, LocalDateTime.now().plusDays(3));

        reservation.cancel();

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
    }

    @Test
    void cancel_confirmedReservation_becomesCANCELLED() {
        Reservation reservation = build(ReservationStatus.CONFIRMED, LocalDateTime.now().plusDays(3));

        reservation.cancel();

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
    }

    @Test
    void cancel_alreadyCancelled_throwsException() {
        Reservation reservation = build(ReservationStatus.CANCELLED, LocalDateTime.now().plusDays(3));

        assertThatThrownBy(reservation::cancel)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PENDING or CONFIRMED");
    }

    @Test
    void confirm_pendingReservation_becomesCONFIRMED() {
        Reservation reservation = build(ReservationStatus.PENDING, LocalDateTime.now().plusDays(3));

        reservation.confirm();

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    void confirm_alreadyConfirmed_throwsException() {
        Reservation reservation = build(ReservationStatus.CONFIRMED, LocalDateTime.now().plusDays(3));

        assertThatThrownBy(reservation::confirm)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PENDING");
    }

    // -------------------------------------------------------------------------

    private Reservation build(ReservationStatus status, LocalDateTime viewingDateTime) {
        return Reservation.builder()
                .id(new ReservationId(1L))
                .reservationNumber("RES-2026-0001")
                .status(status)
                .propertyId(1L)
                .customerName("Test Customer")
                .customerEmail("test@example.com")
                .schedule(new ReservationSchedule(viewingDateTime, 60, null))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
