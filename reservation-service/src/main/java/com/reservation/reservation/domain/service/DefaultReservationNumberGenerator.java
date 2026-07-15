package com.reservation.reservation.domain.service;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultReservationNumberGenerator implements ReservationNumberGenerator {

    private final AtomicInteger sequence = new AtomicInteger(1000);

    @Override
    public String generate() {
        int year = LocalDate.now().getYear();
        int seq = sequence.getAndIncrement();
        return "RES-%d-%04d".formatted(year, seq);
    }
}
