package com.healthcare.appointment.controller;

import com.healthcare.appointment.model.Appointment;
import com.healthcare.appointment.service.PatientBookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/patients")
@PreAuthorize("hasRole('PATIENT')")
public class PatientController {

    @Autowired
    private PatientBookingService bookingService;

    public static class HoldRequest {
        public Long doctorId;
        public Long patientId;
        public LocalDateTime datetime;
        public String symptoms;
        public String aiAnalysis;
    }

    @PostMapping("/hold")
    public ResponseEntity<Appointment> holdSlot(@RequestBody HoldRequest request) {
        Appointment hold = bookingService.holdSlot(request.doctorId, request.patientId, request.datetime, request.symptoms, request.aiAnalysis);
        return ResponseEntity.ok(hold);
    }

    @PostMapping("/confirm/{appointmentId}")
    public ResponseEntity<Appointment> confirmSlot(@PathVariable Long appointmentId, @RequestParam Long patientId) {
        Appointment confirmed = bookingService.confirmBooking(appointmentId, patientId);
        return ResponseEntity.ok(confirmed);
    }

    @Autowired
    private com.healthcare.appointment.repository.PatientProfileRepository patientProfileRepository;

    @Autowired
    private com.healthcare.appointment.repository.MedicationReminderRepository medicationReminderRepository;

    @Autowired
    private com.healthcare.appointment.repository.UserRepository userRepository;

    @GetMapping("/medications")
    public ResponseEntity<?> getMedications(java.security.Principal principal) {
        String email = principal.getName();
        com.healthcare.appointment.model.User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(403).body("User not found");
        }

        com.healthcare.appointment.model.PatientProfile profile = patientProfileRepository.findByUserId(user.getId()).orElse(null);
        if (profile == null) {
            return ResponseEntity.status(403).body("Patient profile not found");
        }

        java.util.List<com.healthcare.appointment.model.MedicationReminder> medications = medicationReminderRepository.findByPatientProfileId(profile.getId());
        return ResponseEntity.ok(medications);
    }
}
