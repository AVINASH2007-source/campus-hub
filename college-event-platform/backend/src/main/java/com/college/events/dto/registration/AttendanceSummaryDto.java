package com.college.events.dto.registration;

public record AttendanceSummaryDto(
        Long eventId,
        String eventTitle,
        int totalRegistered,
        int totalAttended,
        int totalAbsent,
        int totalWaitlisted,
        int totalCancelled,
        double attendancePercentage
) {}