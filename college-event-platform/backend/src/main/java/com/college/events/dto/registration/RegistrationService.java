package com.college.events.service;

import com.college.events.dto.registration.*;
import com.college.events.entity.Event;
import com.college.events.entity.Registration;
import com.college.events.entity.User;
import com.college.events.exception.AccessDeniedException;
import com.college.events.exception.BadRequestException;
import com.college.events.exception.ConflictException;
import com.college.events.exception.ResourceNotFoundException;
import com.college.events.repository.EventRepository;
import com.college.events.repository.RegistrationRepository;
import com.college.events.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    // ── Register ──────────────────────────────────────────────

    @Transactional
    public RegistrationDto register(Long eventId, String email) {
        User user = findUserOrThrow(email);
        Event event = findEventOrThrow(eventId);

        if (!event.isRegistrationOpen()) {
            throw new BadRequestException("Registration is not open for this event.");
        }
        if (!event.hasCapacity()) {
            throw new BadRequestException("Event is fully booked.");
        }
        if (registrationRepository.existsByUserAndEvent(user, event)) {
            throw new ConflictException("You are already registered for this event.");
        }

        Registration registration = Registration.builder()
                .user(user)
                .event(event)
                .status(Registration.RegistrationStatus.CONFIRMED)
                .checkInToken(UUID.randomUUID().toString().replace("-", ""))
                .build();

        registrationRepository.save(registration);

        event.setRegisteredCount(event.getRegisteredCount() + 1);
        eventRepository.save(event);

        log.info("User {} registered for event {}", email, eventId);
        return toDto(registration);
    }

    // ── Cancel ────────────────────────────────────────────────

    @Transactional
    public void cancel(Long registrationId, String email) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Registration", registrationId));

        if (!registration.getUser().getEmail().equals(email)) {
            throw new AccessDeniedException("You can only cancel your own registration.");
        }
        if (registration.getStatus() == Registration.RegistrationStatus.CANCELLED) {
            throw new BadRequestException("Registration is already cancelled.");
        }

        registration.setStatus(Registration.RegistrationStatus.CANCELLED);
        registrationRepository.save(registration);

        Event event = registration.getEvent();
        event.setRegisteredCount(Math.max(0, event.getRegisteredCount() - 1));
        eventRepository.save(event);

        log.info("Registration {} cancelled by {}", registrationId, email);
    }

    // ── My Registrations ──────────────────────────────────────

    public Page<RegistrationDto> findByUser(String email, Pageable pageable) {
        User user = findUserOrThrow(email);
        return registrationRepository.findByUser(user, pageable).map(this::toDto);
    }

    // ── Participants ──────────────────────────────────────────

    public Page<ParticipantDto> findParticipants(
            Long eventId, String status, String organizerEmail, Pageable pageable) {
        Event event = findEventOrThrow(eventId);
        assertOrganizerOrAdmin(event, organizerEmail);
        return registrationRepository
                .findByEventIdAndStatus(eventId, status, pageable)
                .map(this::toParticipantDto);
    }

    // ── Check-in ──────────────────────────────────────────────

    @Transactional
    public RegistrationDto checkIn(String token, String organizerEmail) {
        Registration registration = registrationRepository.findByCheckInToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid check-in token."));

        assertOrganizerOrAdmin(registration.getEvent(), organizerEmail);

        if (registration.getStatus() == Registration.RegistrationStatus.CANCELLED) {
            throw new BadRequestException("Cannot check in a cancelled registration.");
        }

        registration.setAttended(true);
        registration.setCheckedInAt(LocalDateTime.now());
        registration.setStatus(Registration.RegistrationStatus.ATTENDED);
        registrationRepository.save(registration);

        log.info("Check-in successful for token {}", token);
        return toDto(registration);
    }

    // ── Attendance Summary ────────────────────────────────────

    public AttendanceSummaryDto getAttendanceSummary(Long eventId, String organizerEmail) {
        Event event = findEventOrThrow(eventId);
        assertOrganizerOrAdmin(event, organizerEmail);

        long total      = registrationRepository.countByEvent(event);
        long attended   = registrationRepository.countByEventAndAttended(event, true);
        long absent     = total - attended;
        long waitlisted = registrationRepository
                .findByEventIdAndStatus(eventId, "WAITLISTED", Pageable.unpaged())
                .getTotalElements();
        long cancelled  = registrationRepository
                .findByEventIdAndStatus(eventId, "CANCELLED", Pageable.unpaged())
                .getTotalElements();

        double percentage = total > 0 ? (attended * 100.0 / total) : 0.0;

        return new AttendanceSummaryDto(
                eventId,
                event.getTitle(),
                (int) total,
                (int) attended,
                (int) absent,
                (int) waitlisted,
                (int) cancelled,
                Math.round(percentage * 10.0) / 10.0
        );
    }

    // ── Helpers ───────────────────────────────────────────────

    private User findUserOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", email));
    }

    private Event findEventOrThrow(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", id));
    }

    private void assertOrganizerOrAdmin(Event event, String email) {
        User user = findUserOrThrow(email);
        boolean isOwner = event.getOrganizer().getEmail().equals(email);
        boolean isAdmin = user.getRole() == User.Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("Not authorised to manage this event.");
        }
    }

    // ── Mappers ───────────────────────────────────────────────

    private RegistrationDto toDto(Registration r) {
        return new RegistrationDto(
                r.getId(),
                r.getEvent().getId(),
                r.getEvent().getTitle(),
                r.getEvent().getVenue(),
                r.getEvent().getStartTime(),
                r.getUser().getId(),
                r.getUser().getFullName(),
                r.getUser().getEmail(),
                r.getStatus(),
                r.getCheckInToken(),
                r.getAttended(),
                r.getCheckedInAt(),
                r.getRegisteredAt()
        );
    }

    private ParticipantDto toParticipantDto(Registration r) {
        User u = r.getUser();
        return new ParticipantDto(
                r.getId(),
                u.getId(),
                u.getFullName(),
                u.getEmail(),
                u.getRollNumber(),
                u.getDepartment(),
                u.getPhone(),
                r.getStatus(),
                r.getCheckInToken(),
                r.getAttended(),
                r.getCheckedInAt(),
                r.getRegisteredAt()
        );
    }
}