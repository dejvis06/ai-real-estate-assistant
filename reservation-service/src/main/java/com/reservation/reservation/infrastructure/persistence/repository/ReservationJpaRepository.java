package com.reservation.reservation.infrastructure.persistence.repository;

import com.reservation.reservation.infrastructure.persistence.entity.ReservationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationJpaRepository extends JpaRepository<ReservationJpaEntity, Long> {
}
