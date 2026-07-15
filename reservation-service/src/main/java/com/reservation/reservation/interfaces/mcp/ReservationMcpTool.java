package com.reservation.reservation.interfaces.mcp;

import com.reservation.reservation.application.dto.ReservationDetailsResponse;
import com.reservation.reservation.application.service.ReservationApplicationService;
import com.reservation.reservation.application.service.ReservationNotFoundException;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class ReservationMcpTool {

    private final ReservationApplicationService reservationApplicationService;

    public ReservationMcpTool(ReservationApplicationService reservationApplicationService) {
        this.reservationApplicationService = reservationApplicationService;
    }

    @Tool(description = """
            Retrieves reservation details by reservation ID.
            Use this when the customer asks about an existing viewing reservation.
            Returns full reservation information including property details, viewing schedule,
            and evaluated policies (canCancel, canReschedule, cancellationFee, restrictions).
            """)
    public ReservationDetailsResponse getReservation(
            @ToolParam(description = "The numeric ID of the reservation") Long reservationId) {
        try {
            return reservationApplicationService.getReservationDetails(reservationId);
        } catch (ReservationNotFoundException e) {
            throw new IllegalArgumentException("Reservation not found with id: " + reservationId);
        }
    }
}
