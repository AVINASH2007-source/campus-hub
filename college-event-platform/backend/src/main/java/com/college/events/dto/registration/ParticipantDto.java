package com.college.events.dto.registration;

import com.college.events.entity.Registration;
import java.time.LocalDateTime;

public record ParticipantDto(
        Long registrationId,
        Long userId,
        String fullName,
        String email,
        String rollNumber,
        String department,
        String phone,
        Registration.RegistrationStatus status,
        String checkInToken,
        Boolean attended,
        LocalDateTime checkedInAt,
        LocalDateTime registeredAt
) {}
