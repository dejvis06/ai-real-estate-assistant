package com.reservation.config;

import com.reservation.reservation.domain.service.DefaultReservationNumberGenerator;
import com.reservation.reservation.domain.service.ReservationNumberGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class DomainConfig {

    @Bean
    ReservationNumberGenerator reservationNumberGenerator() {
        return new DefaultReservationNumberGenerator();
    }
}
