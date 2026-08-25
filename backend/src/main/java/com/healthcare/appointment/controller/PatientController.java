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
}
