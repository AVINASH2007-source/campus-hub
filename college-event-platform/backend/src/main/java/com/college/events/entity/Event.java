package com.college.events.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Core event entity.
 * Tracks scheduling, capacity, registration window, and fees.
 */
@Entity
@Table(name = "events", indexes = {
        @Index(name = "idx_events_category", columnList = "category"),
        @Index(name = "idx_events_start_time", columnList = "startTime"),
        @Index(name = "idx_events_status", columnList = "status")
})
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Category category;

    @Column(nullable = false, length = 200)
    private String venue;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    /** Null = unlimited capacity */
    private Integer maxParticipants;

    @Column(nullable = false)
    @Builder.Default
    private Integer registeredCount = 0;

    /** Registration opens */
    private LocalDateTime registrationStart;

    /** Registration closes */
    private LocalDateTime registrationDeadline;

    /** 0.00 = free event */
    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal fee = BigDecimal.ZERO;

    @Column(length = 400)
    private String bannerImageUrl;

    /** Tags stored as comma-separated string (e.g. "workshop,ai,beginner") */
    @Column(length = 500)
    private String tags;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EventStatus status = EventStatus.UPCOMING;

    /** The organiser (faculty/admin) who created this event */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Registration> registrations = new HashSet<>();

    // ── Audit ─────────────────────────────────────────────────
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // ── Helpers ───────────────────────────────────────────────
    public boolean hasCapacity() {
        return maxParticipants == null || registeredCount < maxParticipants;
    }

    public boolean isRegistrationOpen() {
        LocalDateTime now = LocalDateTime.now();
        boolean afterStart = registrationStart == null || now.isAfter(registrationStart);
        boolean beforeDeadline = registrationDeadline == null || now.isBefore(registrationDeadline);
        return afterStart && beforeDeadline && status == EventStatus.UPCOMING;
    }

    public int getRemainingSeats() {
        if (maxParticipants == null) return Integer.MAX_VALUE;
        return Math.max(0, maxParticipants - registeredCount);
    }

    public enum Category {
        ACADEMIC, CULTURAL, SPORTS, TECHNICAL, WORKSHOP, SEMINAR, HACKATHON, OTHER
    }

    public enum EventStatus {
        UPCOMING, ONGOING, COMPLETED, CANCELLED
    }
}
