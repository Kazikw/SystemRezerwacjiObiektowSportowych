package com.github.kazikw.boisgo.dto;

import jakarta.validation.constraints.*;

public record UserRegisterRequest(

        @NotBlank(message = "Login jest wymagany.")
        @Size(min = 3, max = 50, message = "Login musi mieć od 3 do 50 znaków.")
        String firstName,

        @NotBlank(message = "Email jest wymagany.")
        @Email(message = "Podaj poprawny adres email.")
        String email,

        @NotBlank(message = "Hasło jest wymagane.")
        @Size(min = 6, message = "Hasło musi mieć co najmniej 6 znaków.")
        String password
) {}