package com.healthcare.appointment.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.jobrunr.jobs.annotations.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class GoogleCalendarService {
    private static final Logger logger = LoggerFactory.getLogger(GoogleCalendarService.class);

    @Value("${google.client.id}")
    private String clientId;

    @Value("${google.client.secret}")
    private String clientSecret;

    @Job(name = "Create Calendar Event for Appointment %0")
    public void createEventForAppointment(Long appointmentId, String userEmail, String refreshToken, String summary, String description, String startTime, String endTime) {
        logger.info("Attempting to create Google Calendar event for appointment {}", appointmentId);
        if (refreshToken == null || refreshToken.isEmpty()) {
            logger.warn("No refresh token provided for user {}", userEmail);
            return; // gracefully fail
        }

        try {
            // Mocking the Google Calendar API call for now since we don't have real credentials
            // In a real implementation we would:
            // 1. Build GoogleCredential using refreshToken, clientId, clientSecret
            // 2. Build Calendar service instance
            // 3. Create Event object with start/end DateTime
            // 4. Call calendar.events().insert("primary", event).execute()
            
            logger.info("MOCK: Successfully created Google Calendar event for {} from {} to {}", summary, startTime, endTime);
            
            // Note: Since we are mocking, we won't save a real google_event_id.
            
        } catch (Exception e) {
            logger.error("Failed to sync to Google Calendar", e);
            throw new RuntimeException("Google Calendar Sync Failed", e); // Throwing so JobRunr can retry
        }
    }

    @Job(name = "Delete Calendar Event %0")
    public void deleteEvent(String googleEventId, String userEmail, String refreshToken) {
        logger.info("Attempting to delete Google Calendar event {}", googleEventId);
        if (googleEventId == null || refreshToken == null) return;
        
        try {
            // Mock delete
            logger.info("MOCK: Successfully deleted event {}", googleEventId);
        } catch (Exception e) {
            logger.error("Failed to delete event", e);
            throw new RuntimeException("Calendar Delete Failed", e);
        }
    }
}
