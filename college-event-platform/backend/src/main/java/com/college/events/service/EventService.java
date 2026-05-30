package com.college.events.service;

import com.college.events.dto.event.*;
import com.college.events.entity.Event;
import com.college.events.entity.User;
import com.college.events.exception.AccessDeniedException;
import com.college.events.exception.BadRequestException;
import com.college.events.exception.ResourceNotFoundException;
import com.college.events.repository.EventRepository;
import com.college.events.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

/**
 * Core event business logic.
 * Organiser ownership is verified on every mutation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventMapper eventMapper;

    // ── Read ──────────────────────────────────────────────────

    public Page<EventSummaryDto> findAll(String category, String status, String search, Pageable pageable) {
        Event.Category cat = category != null ? Event.Category.valueOf(category.toUpperCase()) : null;
        Event.EventStatus stat = status != null ? Event.EventStatus.valueOf(status.toUpperCase()) : null;
        return eventRepository.findWithFilters(cat, stat, search, pageable)
                .map(eventMapper::toSummary);
    }

    public EventDetailDto findById(Long id) {
        return eventMapper.toDetail(findEventOrThrow(id));
    }

    public Page<EventSummaryDto> search(String query, Pageable pageable) {
        return eventRepository.fullTextSearch(query, pageable).map(eventMapper::toSummary);
    }

    public Page<EventSummaryDto> findByOrganizer(String email, Pageable pageable) {
        User organizer = findUserOrThrow(email);
        return eventRepository.findByOrganizer(organizer, pageable).map(eventMapper::toSummary);
    }

    // ── Write ─────────────────────────────────────────────────

    @Transactional
    public EventDetailDto create(CreateEventRequest req, String organizerEmail) {
        validateEventDates(req.startTime(), req.endTime());
        User organizer = findUserOrThrow(organizerEmail);
        Event event = eventMapper.toEntity(req);
        event.setOrganizer(organizer);
        Event saved = eventRepository.save(event);
        log.info("Event '{}' created by {}", saved.getTitle(), organizerEmail);
        return eventMapper.toDetail(saved);
    }

    @Transactional
    public EventDetailDto update(Long id, UpdateEventRequest req, String editorEmail) {
        Event event = findEventOrThrow(id);
        assertOrganizerOrAdmin(event, editorEmail);
        validateEventDates(req.startTime(), req.endTime());
        eventMapper.updateEntity(req, event);
        return eventMapper.toDetail(eventRepository.save(event));
    }

    @Transactional
    public void delete(Long id, String editorEmail) {
        Event event = findEventOrThrow(id);
        assertOrganizerOrAdmin(event, editorEmail);
        if (event.getRegisteredCount() > 0) {
            // Soft-cancel instead of hard delete when participants exist
            event.setStatus(Event.EventStatus.CANCELLED);
            eventRepository.save(event);
        } else {
            eventRepository.delete(event);
        }
        log.info("Event {} cancelled/deleted by {}", id, editorEmail);
    }

    @Transactional
    public EventDetailDto uploadBanner(Long id, MultipartFile file, String uploaderEmail) {
        Event event = findEventOrThrow(id);
        assertOrganizerOrAdmin(event, uploaderEmail);
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path uploadPath = Paths.get("uploads/banners/");
        try {
            Files.createDirectories(uploadPath);
            Files.copy(file.getInputStream(), uploadPath.resolve(filename),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BadRequestException("Could not store banner image: " + e.getMessage());
        }
        event.setBannerImageUrl("/uploads/banners/" + filename);
        return eventMapper.toDetail(eventRepository.save(event));
    }

    // ── Helpers ───────────────────────────────────────────────

    private Event findEventOrThrow(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", id));
    }

    private User findUserOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", email));
    }

    private void assertOrganizerOrAdmin(Event event, String email) {
        User user = findUserOrThrow(email);
        boolean isOwner = event.getOrganizer().getEmail().equals(email);
        boolean isAdmin = user.getRole() == User.Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("You are not authorised to modify this event.");
        }
    }

    private void validateEventDates(java.time.LocalDateTime start, java.time.LocalDateTime end) {
        if (start != null && end != null && !start.isBefore(end)) {
            throw new BadRequestException("Event start time must be before end time.");
        }
    }
}
