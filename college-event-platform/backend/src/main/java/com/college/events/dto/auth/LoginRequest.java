package com.college.events.dto.auth;

import jakarta.validation.constraints.*;

public record LoginRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Enter a valid email")
        String email,

        @NotBlank(message = "Password is required")
        String password
) {}