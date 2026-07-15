package com.reservation.reservation.interfaces.rest;

import com.reservation.reservation.application.dto.CreateReservationRequest;
import com.reservation.reservation.application.dto.ReservationDetailsResponse;
import com.reservation.reservation.application.dto.UpdateReservationRequest;
import com.reservation.reservation.application.service.ReservationApplicationService;
import com.reservation.reservation.application.service.ReservationNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationApplicationService reservationApplicationService;

    public ReservationController(ReservationApplicationService reservationApplicationService) {
        this.reservationApplicationService = reservationApplicationService;
    }

    @PostMapping
    public ResponseEntity<ReservationDetailsResponse> createReservation(
            @RequestBody CreateReservationRequest request) {
        ReservationDetailsResponse response = reservationApplicationService.createReservation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationDetailsResponse> getReservation(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(reservationApplicationService.getReservationDetails(id));
        } catch (ReservationNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReservationDetailsResponse> updateReservation(
            @PathVariable Long id,
            @RequestBody UpdateReservationRequest request) {
        try {
            return ResponseEntity.ok(reservationApplicationService.updateReservation(id, request));
        } catch (ReservationNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
