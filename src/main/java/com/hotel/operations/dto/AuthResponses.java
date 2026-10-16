package com.hotel.operations.dto;

public class AuthResponses {
    public record UserProfile(Long id, String fullName, String email, String role) {}
    public record LoginResponse(String accessToken, UserProfile user) {}
    public record TokenRefreshResponse(String accessToken) {}
}