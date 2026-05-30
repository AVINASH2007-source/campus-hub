package com.college.events.dto.registration;

import com.college.events.entity.Registration;
import java.time.LocalDateTime;

public record RegistrationDto(
        Long id,
        Long eventId,
        String eventTitle,
        String eventVenue,
        LocalDateTime eventStartTime,
        Long userId,
        String userName,
        String userEmail,
        Registration.RegistrationStatus status,
        String checkInToken,
        Boolean attended,
        LocalDateTime checkedInAt,
        LocalDateTime registeredAt
) {}