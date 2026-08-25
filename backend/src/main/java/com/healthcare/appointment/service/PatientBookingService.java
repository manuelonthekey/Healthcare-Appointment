package com.healthcare.appointment.service;

import com.healthcare.appointment.model.Appointment;
import com.healthcare.appointment.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PatientBookingService {
    @Autowired private AppointmentRepository appointmentRepository;
    @Autowired private EmailNotificationService emailNotificationService;
    @Autowired private GoogleCalendarService googleCalendarService;

    @Transactional
    public Appointment holdSlot(Long doctorId, Long patientId, LocalDateTime datetime, String symptoms, String aiAnalysis) {
        Appointment hold = new Appointment();
        hold.setDoctorProfileId(doctorId);
        hold.setPatientProfileId(patientId);
        hold.setAppointmentDatetime(datetime);
        hold.setStatus("HELD");
        hold.setSymptoms(symptoms);
        hold.setAiSummary(aiAnalysis);
        hold.setExpiresAt(LocalDateTime.now().plusMinutes(10)); // 10 minute hold
        
        // This saveAndFlush will throw a DataIntegrityViolationException if another thread
        // has already inserted a row with the exact same (doctor_profile_id, appointment_datetime).
        return appointmentRepository.saveAndFlush(hold);
    }

    @Transactional
    public Appointment confirmBooking(Long appointmentId, Long patientId) {
        Appointment appt = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new RuntimeException("Appointment not found"));
            
        if (!appt.getPatientProfileId().equals(patientId)) {
            throw new RuntimeException("Unauthorized: You do not own this hold");
        }
        if (!"HELD".equals(appt.getStatus())) {
            throw new RuntimeException("Slot is not in HELD state");
        }
        if (appt.getExpiresAt() != null && appt.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Hold has expired");
        }
        
        appt.setStatus("SCHEDULED");
        appt.setExpiresAt(null); // Clear expiry since it's confirmed
        Appointment saved = appointmentRepository.save(appt);

        // Enqueue background jobs for Notifications and Calendar!
        // Using JobRunr BackgroundJob.enqueue for decoupled asynchronous processing
        org.jobrunr.scheduling.BackgroundJob.enqueue(
            () -> emailNotificationService.sendEmail(
                "patient" + patientId + "@example.com", 
                "Booking Confirmed", 
                "Your appointment is confirmed for " + saved.getAppointmentDatetime()
            )
        );
        
        org.jobrunr.scheduling.BackgroundJob.enqueue(
            () -> googleCalendarService.createEventForAppointment(
                saved.getId(), 
                "patient" + patientId + "@example.com", 
                "dummy_refresh_token", 
                "Doctor Appointment", 
                "Symptoms: " + saved.getSymptoms(),
                saved.getAppointmentDatetime().toString(),
                saved.getAppointmentDatetime().plusMinutes(30).toString()
            )
        );

        return saved;
    }
}
