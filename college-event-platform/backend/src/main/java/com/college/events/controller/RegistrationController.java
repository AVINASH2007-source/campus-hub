package com.college.events.controller;

import com.college.events.dto.registration.*;
import com.college.events.service.RegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Handles student event registrations, cancellations,
 * check-in via QR token, and attendance export.
 */
@RestController
@RequestMapping("/api/v1/registrations")
@RequiredArgsConstructor
@Tag(name = "Registrations", description = "Event sign-up and attendance")
public class RegistrationController {

    private final RegistrationService registrationService;

    // ── Student: sign up for an event ─────────────────────────

    @PostMapping("/events/{eventId}")
    @Operation(summary = "Register current user for an event")
    public ResponseEntity<RegistrationDto> register(
            @PathVariable Long eventId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(registrationService.register(eventId, userDetails.getUsername()));
    }

    @DeleteMapping("/{registrationId}")
    @Operation(summary = "Cancel a registration")
    public ResponseEntity<Void> cancel(
            @PathVariable Long registrationId,
            @AuthenticationPrincipal UserDetails userDetails) {
        registrationService.cancel(registrationId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my")
    @Operation(summary = "List all registrations for the current user")
    public ResponseEntity<Page<RegistrationDto>> myRegistrations(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10, sort = "registeredAt") Pageable pageable) {
        return ResponseEntity.ok(registrationService.findByUser(userDetails.getUsername(), pageable));
    }

    // ── Organizer: event participant management ────────────────

    @GetMapping("/events/{eventId}/participants")
    @PreAuthorize("hasAnyRole('FACULTY', 'ADMIN')")
    @Operation(summary = "List all participants registered for an event")
    public ResponseEntity<Page<ParticipantDto>> getParticipants(
            @PathVariable Long eventId,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 50) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                registrationService.findParticipants(eventId, status, userDetails.getUsername(), pageable));
    }

    @PostMapping("/check-in")
    @PreAuthorize("hasAnyRole('FACULTY', 'ADMIN')")
    @Operation(summary = "Mark attendance via QR check-in token")
    public ResponseEntity<RegistrationDto> checkIn(
            @RequestParam String token,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(registrationService.checkIn(token, userDetails.getUsername()));
    }

    @GetMapping("/events/{eventId}/attendance-summary")
    @PreAuthorize("hasAnyRole('FACULTY', 'ADMIN')")
    @Operation(summary = "Attendance stats for a specific event")
    public ResponseEntity<AttendanceSummaryDto> attendanceSummary(
            @PathVariable Long eventId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(registrationService.getAttendanceSummary(eventId, userDetails.getUsername()));
    }
}
