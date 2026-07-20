package com.github.kazikw.boisgo.dto;


import com.github.kazikw.boisgo.domain.Reservation;

public record UserReservationView(
        Reservation reservation,
        boolean owner
) {}