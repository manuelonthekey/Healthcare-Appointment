package com.healthcare.appointment.service;

import com.healthcare.appointment.event.LeaveAddedEvent;
import com.healthcare.appointment.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
public class LeaveConflictListener {

    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private EmailNotificationService emailNotificationService;
    
    @Autowired
    private GoogleCalendarService googleCalendarService;

    @Async
    @EventListener
    @org.springframework.transaction.annotation.Transactional
    public void handleLeaveAddedEvent(LeaveAddedEvent event) {
        LocalDateTime start = event.getLeaveDate().atStartOfDay();
        LocalDateTime end = event.getLeaveDate().atTime(LocalTime.MAX);
        
        // 1. Identify affected appointments
        java.util.List<com.healthcare.appointment.model.Appointment> affected = appointmentRepository
            .findByDoctorProfileIdAndAppointmentDatetimeBetween(event.getDoctorProfileId(), start, end)
            .stream()
            .filter(a -> "SCHEDULED".equals(a.getStatus()) || "HELD".equals(a.getStatus()))
            .toList();

        if (affected.isEmpty()) return;

        // 2. Cancel them in the database
        int cancelled = appointmentRepository.cancelAppointmentsForLeave(
            event.getDoctorProfileId(), start, end
        );
        
        if (cancelled > 0) {
            System.out.println("CRITICAL: Background Job explicitly cancelled " + cancelled + 
                               " appointments due to new leave conflict on " + event.getLeaveDate() +
                               " [Thread: " + Thread.currentThread().getName() + "]");
                               
            // 3. Email Notifications via JobRunr
            for (com.healthcare.appointment.model.Appointment appt : affected) {
                org.jobrunr.scheduling.BackgroundJob.enqueue(
                    () -> emailNotificationService.sendEmail(
                        "patient" + appt.getPatientProfileId() + "@example.com", 
                        "Appointment Cancelled due to Doctor Leave", 
                        "Your appointment on " + appt.getAppointmentDatetime() + " was cancelled due to sudden doctor leave."
                    )
                );
                
                if (appt.getGoogleEventId() != null) {
                    org.jobrunr.scheduling.BackgroundJob.enqueue(
                        () -> googleCalendarService.deleteEvent(appt.getGoogleEventId(), "user@example.com", "dummy_refresh_token")
                    );
                }
            }
        }
    }
}
