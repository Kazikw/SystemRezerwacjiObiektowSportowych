package com.github.kazikw.boisgo.dto;

import com.github.kazikw.boisgo.domain.Reservation;

public record ReservationView(
        Reservation reservation,
        long participantCount,
        boolean canJoin
) {}