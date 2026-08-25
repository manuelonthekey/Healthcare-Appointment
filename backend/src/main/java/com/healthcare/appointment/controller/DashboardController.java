package com.healthcare.appointment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @GetMapping("/metrics")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getMetrics() {
        return ResponseEntity.ok(Map.of(
            "totalAppointments", 145,
            "newAppointments", 12,
            "growth", 23,
            "staff", 57
        ));
    }

    @GetMapping("/today")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getTodayAppointments() {
        // Mock data for UI
        return ResponseEntity.ok(List.of(
            Map.of("id", 1, "patientName", "Marc Joseph", "type", "PCR Test", "time", "11:00 AM"),
            Map.of("id", 2, "patientName", "Kristin Watson", "type", "Consultation", "time", "11:30 AM")
        ));
    }

    @GetMapping("/upcoming")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getUpcomingAppointments() {
        // Mock data for UI
        return ResponseEntity.ok(List.of(
            Map.of("id", 3, "patientName", "Theresa Webb", "type", "PCR Test", "date", "Feb 15", "status", "Postponed"),
            Map.of("id", 4, "patientName", "Darrell Steward", "type", "Antigen Test", "date", "Feb 15", "status", "Completed")
        ));
    }
}
