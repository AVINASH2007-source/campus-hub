package com.college.events.dto.event;

import com.college.events.entity.Event;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EventSummaryDto(
        Long id,
        String title,
        String description,
        Event.Category category,
        String venue,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Integer maxParticipants,
        Integer registeredCount,
        BigDecimal fee,
        String bannerImageUrl,
        String tags,
        Event.EventStatus status,
        String organizerName
) {}