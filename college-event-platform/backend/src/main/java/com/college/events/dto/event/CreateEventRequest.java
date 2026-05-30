package com.college.events.dto.event;

import com.college.events.entity.Event;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateEventRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 200)
        String title,

        String description,

        @NotNull(message = "Category is required")
        Event.Category category,

        @NotBlank(message = "Venue is required")
        String venue,

        @NotNull(message = "Start time is required")
        LocalDateTime startTime,

        @NotNull(message = "End time is required")
        LocalDateTime endTime,

        @Min(value = 1, message = "Capacity must be at least 1")
        Integer maxParticipants,

        LocalDateTime registrationStart,
        LocalDateTime registrationDeadline,

        @DecimalMin(value = "0.0")
        BigDecimal fee,

        String tags
) {}