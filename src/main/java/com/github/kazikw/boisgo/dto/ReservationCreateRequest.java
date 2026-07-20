package com.github.kazikw.boisgo.dto;


import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationCreateRequest(

        @NotNull(message = "Wybierz obiekt.")
        Long facilityId,

        @NotNull(message = "Data rezerwacji jest wymagana.")
        @FutureOrPresent(message = "Data nie może być z przeszłości.")
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        LocalDate date,

        @NotNull(message = "Godzina rozpoczęcia jest wymagana.")
        @DateTimeFormat(pattern = "HH:mm")
        LocalTime startTime,

        boolean groupReservation,

//        @Min(value = 2, message = "Rezerwacja grupowa wymaga co najmniej 2 uczestników.")
//        @Max(value = 30, message = "Zbyt duża liczba uczestników.")
        Integer requiredParticipants,

        boolean allowJoin
) {}