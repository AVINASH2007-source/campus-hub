-- ============================================================
-- College Events Platform — MySQL Schema
-- Run once against an empty `college_events` database.
-- ============================================================

CREATE DATABASE IF NOT EXISTS college_events
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE college_events;

-- ── Users ─────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    id                    BIGINT        NOT NULL AUTO_INCREMENT,
    first_name            VARCHAR(100)  NOT NULL,
    last_name             VARCHAR(100)  NOT NULL,
    email                 VARCHAR(150)  NOT NULL UNIQUE,
    password              VARCHAR(255)  NOT NULL,
    roll_number           VARCHAR(20)   UNIQUE,
    department            VARCHAR(100),
    phone                 VARCHAR(20),
    profile_image_url     VARCHAR(300),
    role                  ENUM('STUDENT','FACULTY','ADMIN') NOT NULL DEFAULT 'STUDENT',
    enabled               TINYINT(1)    NOT NULL DEFAULT 1,
    email_verified        TINYINT(1)    NOT NULL DEFAULT 0,
    verification_token    VARCHAR(64),
    password_reset_token  VARCHAR(64),
    password_reset_expiry DATETIME,
    created_at            DATETIME(6)   NOT NULL,
    updated_at            DATETIME(6),
    PRIMARY KEY (id),
    INDEX idx_users_email      (email),
    INDEX idx_users_roll       (roll_number),
    INDEX idx_users_department (department)
) ENGINE=InnoDB;

-- ── Events ────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS events (
    id                    BIGINT          NOT NULL AUTO_INCREMENT,
    title                 VARCHAR(200)    NOT NULL,
    description           TEXT,
    category              ENUM('ACADEMIC','CULTURAL','SPORTS','TECHNICAL',
                               'WORKSHOP','SEMINAR','HACKATHON','OTHER') NOT NULL,
    venue                 VARCHAR(200)    NOT NULL,
    start_time            DATETIME        NOT NULL,
    end_time              DATETIME        NOT NULL,
    max_participants      INT,
    registered_count      INT             NOT NULL DEFAULT 0,
    registration_start    DATETIME,
    registration_deadline DATETIME,
    fee                   DECIMAL(10, 2)  NOT NULL DEFAULT 0.00,
    banner_image_url      VARCHAR(400),
    tags                  VARCHAR(500),
    status                ENUM('UPCOMING','ONGOING','COMPLETED','CANCELLED') NOT NULL DEFAULT 'UPCOMING',
    organizer_id          BIGINT          NOT NULL,
    created_at            DATETIME(6)     NOT NULL,
    updated_at            DATETIME(6),
    PRIMARY KEY (id),
    INDEX idx_events_category  (category),
    INDEX idx_events_start     (start_time),
    INDEX idx_events_status    (status),
    INDEX idx_events_organizer (organizer_id),
    FULLTEXT INDEX ft_events_search (title, description, tags),
    CONSTRAINT fk_events_organizer FOREIGN KEY (organizer_id) REFERENCES users (id)
) ENGINE=InnoDB;

-- ── Registrations ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS registrations (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    user_id          BIGINT       NOT NULL,
    event_id         BIGINT       NOT NULL,
    status           ENUM('CONFIRMED','WAITLISTED','CANCELLED','ATTENDED') NOT NULL DEFAULT 'CONFIRMED',
    check_in_token   VARCHAR(64)  UNIQUE,
    attended         TINYINT(1),
    checked_in_at    DATETIME,
    notes            VARCHAR(500),
    registered_at    DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_registration_user_event (user_id, event_id),
    INDEX idx_reg_user   (user_id),
    INDEX idx_reg_event  (event_id),
    INDEX idx_reg_status (status),
    CONSTRAINT fk_reg_user  FOREIGN KEY (user_id)  REFERENCES users  (id),
    CONSTRAINT fk_reg_event FOREIGN KEY (event_id) REFERENCES events (id)
) ENGINE=InnoDB;

-- ── Seed Data ─────────────────────────────────────────────────
-- Passwords are BCrypt-hashed "password123" — change before production!
INSERT INTO users (first_name, last_name, email, password, roll_number, department, role, enabled, email_verified, created_at)
VALUES
    ('Admin',   'User',       'admin@college.edu',
     '$2a$12$Z1M4sJGmwXTJL8WkYIlqDumvb3z6YBHiwjPAL9r.JiHlUH6t3EFkS',
     NULL, NULL, 'ADMIN', 1, 1, NOW()),

    ('Priya',   'Sharma',     'priya.sharma@college.edu',
     '$2a$12$Z1M4sJGmwXTJL8WkYIlqDumvb3z6YBHiwjPAL9r.JiHlUH6t3EFkS',
     NULL, 'Computer Science', 'FACULTY', 1, 1, NOW()),

    ('Rahul',   'Verma',      'rahul.verma@college.edu',
     '$2a$12$Z1M4sJGmwXTJL8WkYIlqDumvb3z6YBHiwjPAL9r.JiHlUH6t3EFkS',
     'CS2021001', 'Computer Science', 'STUDENT', 1, 1, NOW()),

    ('Anjali',  'Patel',      'anjali.patel@college.edu',
     '$2a$12$Z1M4sJGmwXTJL8WkYIlqDumvb3z6YBHiwjPAL9r.JiHlUH6t3EFkS',
     'IT2022010', 'Information Technology', 'STUDENT', 1, 1, NOW());

-- Sample events (organizer_id = 2 = Priya Sharma / faculty)
INSERT INTO events (title, description, category, venue, start_time, end_time,
                    max_participants, registration_deadline, fee, tags, status, organizer_id, created_at)
VALUES
    ('National AI Hackathon 2025',
     'Build AI-powered solutions in 24 hours. Teams of 3-4. Prizes worth ₹1,00,000.',
     'HACKATHON', 'Main Auditorium',
     DATE_ADD(NOW(), INTERVAL 14 DAY), DATE_ADD(NOW(), INTERVAL 15 DAY),
     100, DATE_ADD(NOW(), INTERVAL 10 DAY), 0.00,
     'ai,machine-learning,hackathon,coding', 'UPCOMING', 2, NOW()),

    ('ReactJS Workshop — Build Modern UIs',
     'Hands-on workshop covering React hooks, context, and performance optimisation.',
     'WORKSHOP', 'CS Lab 3',
     DATE_ADD(NOW(), INTERVAL 7 DAY), DATE_ADD(NOW(), INTERVAL 7 DAY) + INTERVAL 3 HOUR,
     30, DATE_ADD(NOW(), INTERVAL 5 DAY), 200.00,
     'react,javascript,frontend,workshop', 'UPCOMING', 2, NOW()),

    ('Annual Sports Day 2025',
     'Inter-department athletics, cricket, and chess tournament.',
     'SPORTS', 'College Ground',
     DATE_ADD(NOW(), INTERVAL 21 DAY), DATE_ADD(NOW(), INTERVAL 22 DAY),
     500, DATE_ADD(NOW(), INTERVAL 18 DAY), 0.00,
     'sports,athletics,cricket,chess', 'UPCOMING', 2, NOW());
