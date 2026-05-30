    package com.college.events.dto.event;

    import com.college.events.entity.Event;
    import jakarta.validation.constraints.*;
    import java.math.BigDecimal;
    import java.time.LocalDateTime;

    public record UpdateEventRequest(
            @Size(max = 200)
            String title,

            String description,
            Event.Category category,
            String venue,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Integer maxParticipants,
            LocalDateTime registrationStart,
            LocalDateTime registrationDeadline,
            BigDecimal fee,
            String tags,
            Event.EventStatus status
    ) {}