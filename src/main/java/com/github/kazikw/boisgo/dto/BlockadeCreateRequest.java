package com.github.kazikw.boisgo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record BlockadeCreateRequest(

        @NotNull(message = "Wybierz obiekt.")
        Long facilityId,

        @NotNull(message = "Data rozpoczęcia jest wymagana.")
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        LocalDate startDate,

        @NotNull(message = "Data zakończenia jest wymagana.")
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        LocalDate endDate,

        @NotBlank(message = "Podaj powód blokady.")
        String reason
) {}