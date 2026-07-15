package com.reservation.reservation.domain.model;

import java.time.LocalDateTime;

public class ReservationSchedule {

    private final LocalDateTime viewingDateTime;
    private final Integer durationMinutes;
    private final String agentNotes;

    public ReservationSchedule(LocalDateTime viewingDateTime, Integer durationMinutes, String agentNotes) {
        this.viewingDateTime = viewingDateTime;
        this.durationMinutes = durationMinutes;
        this.agentNotes = agentNotes;
    }

    public LocalDateTime getViewingDateTime() { return viewingDateTime; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public String getAgentNotes() { return agentNotes; }
}
