package com.hotel.operations.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AuthRequests {
    public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {}
}