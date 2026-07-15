package com.reservation.reservation.application.dto;

import com.reservation.reservation.domain.model.ReservationSchedule;

import java.time.LocalDateTime;

public record ReservationScheduleResponse(
        LocalDateTime viewingDateTime,
        Integer durationMinutes,
        String agentNotes
) {

    public static ReservationScheduleResponse from(ReservationSchedule schedule) {
        if (schedule == null) return null;
        return new ReservationScheduleResponse(
                schedule.getViewingDateTime(),
                schedule.getDurationMinutes(),
                schedule.getAgentNotes()
        );
    }
}
