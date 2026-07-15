package com.reservation.reservation.infrastructure.persistence.repository;

import com.reservation.reservation.domain.model.Reservation;
import com.reservation.reservation.domain.model.ReservationId;
import com.reservation.reservation.domain.repository.ReservationRepository;
import com.reservation.reservation.infrastructure.persistence.mapper.ReservationMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ReservationRepositoryImpl implements ReservationRepository {

    private final ReservationJpaRepository jpaRepository;
    private final ReservationMapper mapper;

    public ReservationRepositoryImpl(ReservationJpaRepository jpaRepository, ReservationMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Reservation> findById(ReservationId id) {
        return jpaRepository.findById(id.value())
                .map(mapper::toDomain);
    }

    @Override
    public Reservation save(Reservation reservation) {
        var entity = mapper.toEntity(reservation);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
}
