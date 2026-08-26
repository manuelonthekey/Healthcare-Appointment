package com.healthcare.appointment.controller;

import com.healthcare.appointment.model.Appointment;
import com.healthcare.appointment.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import com.healthcare.appointment.service.EmailNotificationService;
import com.healthcare.appointment.service.GoogleCalendarService;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private EmailNotificationService emailNotificationService;
    
    @Autowired
    private GoogleCalendarService googleCalendarService;

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR') or hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<?> cancelAppointment(@PathVariable Long id) {
        Appointment appt = appointmentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Appointment not found"));
            
        if ("CANCELLED".equals(appt.getStatus()) || "COMPLETED".equals(appt.getStatus())) {
            return ResponseEntity.badRequest().body("Cannot cancel an appointment that is already " + appt.getStatus());
        }

        appt.setStatus("CANCELLED");
        appt.setExpiresAt(null);
        appointmentRepository.save(appt);
        
        // Enqueue background jobs for Notifications and Calendar
        org.jobrunr.scheduling.BackgroundJob.enqueue(
            () -> emailNotificationService.sendEmail(
                "patient" + appt.getPatientProfileId() + "@example.com", 
                "Appointment Cancelled", 
                "Your appointment on " + appt.getAppointmentDatetime() + " has been cancelled."
            )
        );

        if (appt.getGoogleEventId() != null) {
            org.jobrunr.scheduling.BackgroundJob.enqueue(
                () -> googleCalendarService.deleteEvent(appt.getGoogleEventId(), "user@example.com", "dummy_refresh_token")
            );
        }

        System.out.println("CRITICAL: Appointment " + id + " was cancelled.");
        
        return ResponseEntity.ok(appt);
    }

    public static class CompleteRequest {
        public String clinicalNotes;
        public String aiSummary;
        public java.util.List<com.healthcare.appointment.dto.ClinicalSummaryResponse.MedicationDto> medications;
    }

    @Autowired
    private com.healthcare.appointment.repository.MedicationReminderRepository medicationReminderRepository;

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<?> completeAppointment(@PathVariable Long id, @RequestBody CompleteRequest req) {
        Appointment appt = appointmentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Appointment not found"));
            
        if (!"SCHEDULED".equals(appt.getStatus())) {
            return ResponseEntity.badRequest().body("Cannot complete an appointment that is not SCHEDULED");
        }

        appt.setStatus("COMPLETED");
        appt.setClinicalNotes(req.clinicalNotes);
        appt.setAiSummary(req.aiSummary);
        appt.setCompletedAt(LocalDateTime.now());
        
        appointmentRepository.save(appt);
        
        if (req.medications != null && !req.medications.isEmpty()) {
            for (com.healthcare.appointment.dto.ClinicalSummaryResponse.MedicationDto med : req.medications) {
                if (med.getTimes() != null && !med.getTimes().isEmpty()) {
                    for (String t : med.getTimes()) {
                        try {
                            java.time.LocalTime parsedTime = java.time.LocalTime.parse(t);
                            if (medicationReminderRepository.existsByAppointmentIdAndMedicationNameAndReminderTime(appt.getId(), med.getName(), parsedTime)) {
                                continue; // Idempotency check
                            }
                            com.healthcare.appointment.model.MedicationReminder reminder = new com.healthcare.appointment.model.MedicationReminder();
                            reminder.setAppointmentId(appt.getId());
                            reminder.setPatientProfileId(appt.getPatientProfileId());
                            reminder.setMedicationName(med.getName());
                            reminder.setDosage(med.getDosage());
                            reminder.setFrequency(med.getFrequency());
                            reminder.setReminderTime(parsedTime);
                            reminder.setStartDate(med.getStartDate() != null ? java.time.LocalDate.parse(med.getStartDate()) : java.time.LocalDate.now());
                            reminder.setEndDate(med.getEndDate() != null ? java.time.LocalDate.parse(med.getEndDate()) : java.time.LocalDate.now().plusDays(7));
                            medicationReminderRepository.save(reminder);
                        } catch (Exception e) {
                            System.err.println("Failed to parse medication time/date: " + e.getMessage());
                        }
                    }
                }
            }
        }
        
        System.out.println("CRITICAL: Appointment " + id + " was completed.");
        
        return ResponseEntity.ok(appt);
    }
}
