package com.college.events.dto.auth;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        UserDto user
) {
    public record UserDto(
            Long id,
            String firstName,
            String lastName,
            String email,
            String role,
            String department,
            String rollNumber
    ) {}
}