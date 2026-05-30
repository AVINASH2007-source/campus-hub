package com.college.events.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a platform user — student, faculty, or admin.
 * Roles drive access control across all endpoints.
 */
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_email", columnList = "email"),
        @Index(name = "idx_users_roll_number", columnList = "rollNumber")
})
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false)
    private String password;

    /** e.g. "CS2021001" — unique per college */
    @Column(unique = true, length = 20)
    private String rollNumber;

    @Column(length = 100)
    private String department;

    @Column(length = 20)
    private String phone;

    @Column(length = 300)
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.STUDENT;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    @Column(length = 64)
    private String verificationToken;

    @Column(length = 64)
    private String passwordResetToken;

    private LocalDateTime passwordResetExpiry;

    // ── Registrations ─────────────────────────────────────────
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Registration> registrations = new HashSet<>();

    // ── Audit ─────────────────────────────────────────────────
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // ── Helpers ───────────────────────────────────────────────
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public enum Role {
        STUDENT, FACULTY, ADMIN
    }
}
