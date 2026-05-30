package com.college.events.controller;

import com.college.events.dto.event.*;
import com.college.events.entity.Event;
import com.college.events.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Event management endpoints.
 * Public read-only + authenticated create/update/delete for organizers.
 */
@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@Tag(name = "Events", description = "Event creation, browsing, and management")
public class EventController {

    private final EventService eventService;

    // ── Public Endpoints ──────────────────────────────────────

    @GetMapping
    @Operation(summary = "List events with pagination and optional filters")
    public ResponseEntity<Page<EventSummaryDto>> listEvents(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 12, sort = "startTime", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(eventService.findAll(category, status, search, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get full event details by ID")
    public ResponseEntity<EventDetailDto> getEvent(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.findById(id));
    }

    @GetMapping("/categories")
    @Operation(summary = "List all available event categories")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(
                List.of(Event.Category.values()).stream()
                        .map(Enum::name).toList()
        );
    }

    @GetMapping("/search")
    @Operation(summary = "Full-text search across title, description, and tags")
    public ResponseEntity<Page<EventSummaryDto>> searchEvents(
            @RequestParam String q,
            @PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok(eventService.search(q, pageable));
    }

    // ── Organizer Endpoints ───────────────────────────────────

    @PostMapping
    @PreAuthorize("hasAnyRole('FACULTY', 'ADMIN')")
    @Operation(summary = "Create a new event")
    public ResponseEntity<EventDetailDto> createEvent(
            @Valid @RequestBody CreateEventRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(eventService.create(request, userDetails.getUsername()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('FACULTY', 'ADMIN')")
    @Operation(summary = "Update an existing event")
    public ResponseEntity<EventDetailDto> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEventRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(eventService.update(id, request, userDetails.getUsername()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('FACULTY', 'ADMIN')")
    @Operation(summary = "Cancel / delete an event")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        eventService.delete(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/banner")
    @PreAuthorize("hasAnyRole('FACULTY', 'ADMIN')")
    @Operation(summary = "Upload event banner image")
    public ResponseEntity<EventDetailDto> uploadBanner(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(eventService.uploadBanner(id, file, userDetails.getUsername()));
    }

    // ── My Events (organizer view) ────────────────────────────

    @GetMapping("/my-events")
    @PreAuthorize("hasAnyRole('FACULTY', 'ADMIN')")
    @Operation(summary = "Events created by the authenticated organizer")
    public ResponseEntity<Page<EventSummaryDto>> myEvents(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(eventService.findByOrganizer(userDetails.getUsername(), pageable));
    }
}
