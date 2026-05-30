package com.college.events.repository;

import com.college.events.entity.Registration;
import com.college.events.entity.User;
import com.college.events.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    Optional<Registration> findByCheckInToken(String token);
    Optional<Registration> findByUserAndEvent(User user, Event event);
    boolean existsByUserAndEvent(User user, Event event);
    Page<Registration> findByUser(User user, Pageable pageable);
    Page<Registration> findByEvent(Event event, Pageable pageable);

    @Query("""
        SELECT r FROM Registration r
        WHERE r.event.id = :eventId
          AND (:status IS NULL OR CAST(r.status AS string) = :status)
        """)
    Page<Registration> findByEventIdAndStatus(
            @Param("eventId") Long eventId,
            @Param("status") String status,
            Pageable pageable);

    long countByEvent(Event event);
    long countByEventAndAttended(Event event, Boolean attended);
}