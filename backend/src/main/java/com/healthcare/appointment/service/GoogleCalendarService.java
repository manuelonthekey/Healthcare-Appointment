package com.healthcare.appointment.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private com.healthcare.appointment.repository.AppointmentRepository appointmentRepository;

    @Job(name = "Create Calendar Event for Appointment %0")
    public void createEventForAppointment(Long appointmentId, String userEmail, String refreshToken, String summary, String description, String startTime, String endTime) {
        logger.info("Attempting to create Google Calendar event for appointment {}", appointmentId);
        if (refreshToken == null || refreshToken.isEmpty()) {
            logger.warn("No refresh token provided for user {}", userEmail);
            return;
        }
        if (clientId == null || clientId.equals("dummy-id")) {
             logger.warn("Google client ID not configured.");
             return;
        }

        try {
            Credential credential = new GoogleCredential.Builder()
                .setTransport(new NetHttpTransport())
                .setJsonFactory(GsonFactory.getDefaultInstance())
                .setClientSecrets(clientId, clientSecret)
                .build()
                .setRefreshToken(refreshToken);

            Calendar service = new Calendar.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance(), credential)
                .setApplicationName("Healthcare Appointment System")
                .build();

            Event event = new Event()
                .setSummary(summary)
                .setDescription(description);

            EventDateTime start = new EventDateTime()
                .setDateTime(new DateTime(startTime));
            event.setStart(start);

            EventDateTime end = new EventDateTime()
                .setDateTime(new DateTime(endTime));
            event.setEnd(end);

            Event createdEvent = service.events().insert("primary", event).execute();
            logger.info("Successfully created Google Calendar event: {}", createdEvent.getId());
            
            appointmentRepository.findById(appointmentId).ifPresent(appt -> {
                appt.setGoogleEventId(createdEvent.getId());
                appointmentRepository.save(appt);
            });
            
        } catch (Exception e) {
            logger.error("Failed to sync to Google Calendar", e);
            throw new RuntimeException("Google Calendar Sync Failed", e);
        }
    }

    @Job(name = "Delete Calendar Event %0")
    public void deleteEvent(String googleEventId, String userEmail, String refreshToken) {
        logger.info("Attempting to delete Google Calendar event {}", googleEventId);
        if (googleEventId == null || refreshToken == null) return;
        if (clientId == null || clientId.equals("dummy-id")) return;
        
        try {
            Credential credential = new GoogleCredential.Builder()
                .setTransport(new NetHttpTransport())
                .setJsonFactory(GsonFactory.getDefaultInstance())
                .setClientSecrets(clientId, clientSecret)
                .build()
                .setRefreshToken(refreshToken);

            Calendar service = new Calendar.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance(), credential)
                .setApplicationName("Healthcare Appointment System")
                .build();

            service.events().delete("primary", googleEventId).execute();
            logger.info("Successfully deleted event {}", googleEventId);
        } catch (Exception e) {
            logger.error("Failed to delete event", e);
            throw new RuntimeException("Calendar Delete Failed", e);
        }
    }
}
