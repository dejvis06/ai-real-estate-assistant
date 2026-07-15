package com.reservation.reservation.domain.repository;

import com.reservation.reservation.domain.model.Reservation;
import com.reservation.reservation.domain.model.ReservationId;

import java.util.Optional;

public interface ReservationRepository {

    Optional<Reservation> findById(ReservationId id);

    Reservation save(Reservation reservation);
}
