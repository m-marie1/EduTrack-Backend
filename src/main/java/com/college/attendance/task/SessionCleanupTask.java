package com.college.attendance.task;

import com.college.attendance.service.AttendanceSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SessionCleanupTask {

    private final AttendanceSessionService attendanceSessionService;

    // Run every 15 minutes (fixed rate)
    // cron = "0 */15 * * * *" -> runs at 0, 15, 30, 45 minutes past the hour
    // fixedRate = 900000 -> runs every 15 minutes after the application starts
    @Scheduled(fixedRate = 900000) // 15 minutes * 60 seconds * 1000 ms
    public void deactivateExpiredSessions() {
        log.info("Running scheduled task to deactivate expired attendance sessions...");
        try {
            int deactivatedCount = attendanceSessionService.deactivateExpiredSessions();
            if (deactivatedCount > 0) {
                log.info("Successfully deactivated {} expired attendance sessions.", deactivatedCount);
            } else {
                log.info("No expired attendance sessions found to deactivate.");
            }
        } catch (Exception e) {
            log.error("Error during scheduled deactivation of expired sessions: {}", e.getMessage(), e);
        }
    }
}