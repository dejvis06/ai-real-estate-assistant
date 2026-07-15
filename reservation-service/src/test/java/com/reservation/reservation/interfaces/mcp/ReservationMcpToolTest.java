package com.reservation.reservation.interfaces.mcp;

import com.reservation.reservation.application.dto.ReservationDetailsResponse;
import com.reservation.reservation.application.dto.ReservationPolicyResponse;
import com.reservation.reservation.application.dto.ReservationScheduleResponse;
import com.reservation.reservation.application.service.ReservationApplicationService;
import com.reservation.reservation.application.service.ReservationNotFoundException;
import com.reservation.reservation.domain.model.ReservationStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationMcpToolTest {

    @Mock
    private ReservationApplicationService reservationApplicationService;

    @InjectMocks
    private ReservationMcpTool reservationMcpTool;

    @Test
    void getReservation_existingId_returnsDetails() {
        ReservationDetailsResponse response = new ReservationDetailsResponse(
                1L,
                "RES-2026-0001",
                ReservationStatus.CONFIRMED,
                null,
                new ReservationScheduleResponse(LocalDateTime.now().plusDays(3), 60, null),
                new ReservationPolicyResponse(true, true, LocalDateTime.now().plusDays(2), null, List.of()),
                "Interested in buying.",
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        when(reservationApplicationService.getReservationDetails(1L)).thenReturn(response);

        ReservationDetailsResponse result = reservationMcpTool.getReservation(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.reservationNumber()).isEqualTo("RES-2026-0001");
        assertThat(result.status()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(result.policy().canCancel()).isTrue();
        assertThat(result.policy().canReschedule()).isTrue();
    }

    @Test
    void getReservation_notFound_throwsIllegalArgumentException() {
        when(reservationApplicationService.getReservationDetails(99L))
                .thenThrow(new ReservationNotFoundException(99L));

        assertThatThrownBy(() -> reservationMcpTool.getReservation(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }
}
