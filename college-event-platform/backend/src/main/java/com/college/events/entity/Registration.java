package com.college.events.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Join table between User and Event.
 * Tracks registration status, attendance, and payment.
 */
@Entity
@Table(name = "registrations",
       uniqueConstraints = @UniqueConstraint(
               name = "uk_registration_user_event",
               columnNames = {"user_id", "event_id"}
       ),
       indexes = {
               @Index(name = "idx_reg_user", columnList = "user_id"),
               @Index(name = "idx_reg_event", columnList = "event_id"),
               @Index(name = "idx_reg_status", columnList = "status")
       })
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RegistrationStatus status = RegistrationStatus.CONFIRMED;

    /** QR code or unique check-in token */
    @Column(unique = true, length = 64)
    private String checkInToken;

    private Boolean attended;

    private LocalDateTime checkedInAt;

    /** Optional notes from the organizer */
    @Column(length = 500)
    private String notes;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime registeredAt;

    public enum RegistrationStatus {
        CONFIRMED, WAITLISTED, CANCELLED, ATTENDED
    }
}
