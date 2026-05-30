package com.college.events.repository;

import com.college.events.entity.Event;
import com.college.events.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    /**
     * Flexible listing with optional category, status, and keyword filters.
     * All three params are nullable — absent params are ignored.
     */
    @Query("""
        SELECT e FROM Event e
        WHERE (:category IS NULL OR e.category = :category)
          AND (:status   IS NULL OR e.status   = :status)
          AND (:search   IS NULL
               OR LOWER(e.title)       LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(e.description) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(e.tags)        LIKE LOWER(CONCAT('%', :search, '%')))
        """)
    Page<Event> findWithFilters(
            @Param("category") Event.Category category,
            @Param("status")   Event.EventStatus status,
            @Param("search")   String search,
            Pageable pageable);

    /** Dedicated full-text search across title + description + tags. */
    @Query("""
        SELECT e FROM Event e
        WHERE LOWER(e.title)       LIKE LOWER(CONCAT('%', :q, '%'))
           OR LOWER(e.description) LIKE LOWER(CONCAT('%', :q, '%'))
           OR LOWER(e.tags)        LIKE LOWER(CONCAT('%', :q, '%'))
        """)
    Page<Event> fullTextSearch(@Param("q") String query, Pageable pageable);

    Page<Event> findByOrganizer(User organizer, Pageable pageable);

    /** Upcoming events within a date window (used for reminders). */
    @Query("SELECT e FROM Event e WHERE e.startTime BETWEEN :from AND :to AND e.status = 'UPCOMING'")
    Page<Event> findUpcomingBetween(
            @Param("from") LocalDateTime from,
            @Param("to")   LocalDateTime to,
            Pageable pageable);

    /** Events with open registration and remaining capacity. */
    @Query("""
        SELECT e FROM Event e
        WHERE e.status = 'UPCOMING'
          AND (e.registrationDeadline IS NULL OR e.registrationDeadline > :now)
          AND (e.registrationStart    IS NULL OR e.registrationStart    < :now)
          AND (e.maxParticipants      IS NULL OR e.registeredCount < e.maxParticipants)
        """)
    Page<Event> findOpenForRegistration(@Param("now") LocalDateTime now, Pageable pageable);

    Optional<Event> findByIdAndOrganizerEmail(Long id, String organizerEmail);

    long countByOrganizer(User organizer);

    long countByStatus(Event.EventStatus status);
}
