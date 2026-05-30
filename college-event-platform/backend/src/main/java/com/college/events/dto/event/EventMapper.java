package com.college.events.dto.event;

import com.college.events.entity.Event;
import org.springframework.stereotype.Component;

@Component
public class EventMapper {

    public EventSummaryDto toSummary(Event event) {
        return new EventSummaryDto(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getCategory(),
                event.getVenue(),
                event.getStartTime(),
                event.getEndTime(),
                event.getMaxParticipants(),
                event.getRegisteredCount(),
                event.getFee(),
                event.getBannerImageUrl(),
                event.getTags(),
                event.getStatus(),
                event.getOrganizer().getFullName()
        );
    }

    public EventDetailDto toDetail(Event event) {
        return new EventDetailDto(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getCategory(),
                event.getVenue(),
                event.getStartTime(),
                event.getEndTime(),
                event.getMaxParticipants(),
                event.getRegisteredCount(),
                event.getRegistrationStart(),
                event.getRegistrationDeadline(),
                event.getFee(),
                event.getBannerImageUrl(),
                event.getTags(),
                event.getStatus(),
                event.getOrganizer().getFullName(),
                event.getOrganizer().getEmail(),
                event.isRegistrationOpen(),
                event.getRemainingSeats(),
                event.getCreatedAt()
        );
    }

    public Event toEntity(CreateEventRequest req) {
        return Event.builder()
                .title(req.title())
                .description(req.description())
                .category(req.category())
                .venue(req.venue())
                .startTime(req.startTime())
                .endTime(req.endTime())
                .maxParticipants(req.maxParticipants())
                .registrationStart(req.registrationStart())
                .registrationDeadline(req.registrationDeadline())
                .fee(req.fee() != null ? req.fee() : java.math.BigDecimal.ZERO)
                .tags(req.tags())
                .build();
    }

    public void updateEntity(UpdateEventRequest req, Event event) {
        if (req.title()               != null) event.setTitle(req.title());
        if (req.description()         != null) event.setDescription(req.description());
        if (req.category()            != null) event.setCategory(req.category());
        if (req.venue()               != null) event.setVenue(req.venue());
        if (req.startTime()           != null) event.setStartTime(req.startTime());
        if (req.endTime()             != null) event.setEndTime(req.endTime());
        if (req.maxParticipants()     != null) event.setMaxParticipants(req.maxParticipants());
        if (req.registrationStart()   != null) event.setRegistrationStart(req.registrationStart());
        if (req.registrationDeadline()!= null) event.setRegistrationDeadline(req.registrationDeadline());
        if (req.fee()                 != null) event.setFee(req.fee());
        if (req.tags()                != null) event.setTags(req.tags());
        if (req.status()              != null) event.setStatus(req.status());
    }
}