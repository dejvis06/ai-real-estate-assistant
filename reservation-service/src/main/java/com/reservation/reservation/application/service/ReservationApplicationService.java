package com.reservation.reservation.application.service;

import com.reservation.reservation.application.dto.CreateReservationRequest;
import com.reservation.reservation.application.dto.PropertyResponse;
import com.reservation.reservation.application.dto.ReservationDetailsResponse;
import com.reservation.reservation.application.dto.UpdateReservationRequest;
import com.reservation.reservation.application.port.PropertyServiceClient;
import com.reservation.reservation.domain.model.Reservation;
import com.reservation.reservation.domain.model.ReservationId;
import com.reservation.reservation.domain.model.ReservationPolicy;
import com.reservation.reservation.domain.model.ReservationSchedule;
import com.reservation.reservation.domain.model.ReservationStatus;
import com.reservation.reservation.domain.repository.ReservationRepository;
import com.reservation.reservation.domain.service.ReservationNumberGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ReservationApplicationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationApplicationService.class);

    private final ReservationRepository reservationRepository;
    private final PropertyServiceClient propertyServiceClient;
    private final ReservationNumberGenerator reservationNumberGenerator;

    public ReservationApplicationService(ReservationRepository reservationRepository,
                                         PropertyServiceClient propertyServiceClient,
                                         ReservationNumberGenerator reservationNumberGenerator) {
        this.reservationRepository = reservationRepository;
        this.propertyServiceClient = propertyServiceClient;
        this.reservationNumberGenerator = reservationNumberGenerator;
    }

    public ReservationDetailsResponse getReservationDetails(Long id) {
        Reservation reservation = reservationRepository.findById(new ReservationId(id))
                .orElseThrow(() -> new ReservationNotFoundException(id));

        PropertyResponse property = propertyServiceClient.findById(reservation.getPropertyId())
                .orElse(null);

        ReservationPolicy policy = reservation.evaluatePolicy();

        log.info("getReservationDetails [{}]: found, status={}", id, reservation.getStatus());
        return ReservationDetailsResponse.from(reservation, property, policy);
    }

    @Transactional
    public ReservationDetailsResponse createReservation(CreateReservationRequest request) {
        ReservationSchedule schedule = new ReservationSchedule(
                request.viewingDateTime(),
                request.durationMinutes(),
                null
        );

        Reservation reservation = Reservation.builder()
                .reservationNumber(reservationNumberGenerator.generate())
                .status(ReservationStatus.PENDING)
                .propertyId(request.propertyId())
                .customerName(request.customerName())
                .customerEmail(request.customerEmail())
                .customerPhone(request.customerPhone())
                .schedule(schedule)
                .customerMessage(request.customerMessage())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Reservation saved = reservationRepository.save(reservation);

        PropertyResponse property = propertyServiceClient.findById(saved.getPropertyId()).orElse(null);
        ReservationPolicy policy = saved.evaluatePolicy();

        log.info("createReservation: created [{}] for propertyId={}", saved.getReservationNumber(), saved.getPropertyId());
        return ReservationDetailsResponse.from(saved, property, policy);
    }

    @Transactional
    public ReservationDetailsResponse updateReservation(Long id, UpdateReservationRequest request) {
        Reservation reservation = reservationRepository.findById(new ReservationId(id))
                .orElseThrow(() -> new ReservationNotFoundException(id));

        ReservationSchedule updatedSchedule = new ReservationSchedule(
                Optional.ofNullable(request.viewingDateTime())
                        .orElse(reservation.getSchedule() != null ? reservation.getSchedule().getViewingDateTime() : null),
                Optional.ofNullable(request.durationMinutes())
                        .orElse(reservation.getSchedule() != null ? reservation.getSchedule().getDurationMinutes() : null),
                Optional.ofNullable(request.agentNotes())
                        .orElse(reservation.getSchedule() != null ? reservation.getSchedule().getAgentNotes() : null)
        );

        Reservation updated = Reservation.builder()
                .id(reservation.getId())
                .reservationNumber(reservation.getReservationNumber())
                .status(Optional.ofNullable(request.status()).orElse(reservation.getStatus()))
                .propertyId(reservation.getPropertyId())
                .customerName(reservation.getCustomerName())
                .customerEmail(reservation.getCustomerEmail())
                .customerPhone(reservation.getCustomerPhone())
                .schedule(updatedSchedule)
                .customerMessage(reservation.getCustomerMessage())
                .internalNotes(Optional.ofNullable(request.internalNotes()).orElse(reservation.getInternalNotes()))
                .createdAt(reservation.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        Reservation saved = reservationRepository.save(updated);

        PropertyResponse property = propertyServiceClient.findById(saved.getPropertyId()).orElse(null);
        ReservationPolicy policy = saved.evaluatePolicy();

        log.info("updateReservation [{}]: status={}", id, saved.getStatus());
        return ReservationDetailsResponse.from(saved, property, policy);
    }
}
