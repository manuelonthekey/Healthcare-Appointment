package com.healthcare.appointment.controller;

import com.healthcare.appointment.model.DoctorAvailability;
import com.healthcare.appointment.model.DoctorLeave;
import com.healthcare.appointment.repository.DoctorAvailabilityRepository;
import com.healthcare.appointment.repository.DoctorLeaveRepository;
import com.healthcare.appointment.service.SlotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    @Autowired private DoctorAvailabilityRepository availabilityRepository;
    @Autowired private DoctorLeaveRepository leaveRepository;
    @Autowired private SlotService slotService;

    @PostMapping("/{profileId}/availability")
    @PreAuthorize("hasRole('DOCTOR')")
    public DoctorAvailability addAvailability(@PathVariable Long profileId, @RequestBody DoctorAvailability availability) {
        availability.setDoctorProfileId(profileId);
        return availabilityRepository.save(availability);
    }

    @Autowired private ApplicationEventPublisher eventPublisher;

    @PostMapping("/{profileId}/leaves")
    @PreAuthorize("hasRole('DOCTOR')")
    public DoctorLeave addLeave(@PathVariable Long profileId, @RequestBody DoctorLeave leave) {
        leave.setDoctorProfileId(profileId);
        DoctorLeave saved = leaveRepository.save(leave);
        eventPublisher.publishEvent(new com.healthcare.appointment.event.LeaveAddedEvent(this, profileId, leave.getLeaveDate()));
        return saved;
    }

    // Public endpoint for patients to check slots
    @GetMapping("/{profileId}/slots")
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<List<LocalTime>> getAvailableSlots(
            @PathVariable Long profileId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<LocalTime> slots = slotService.generateAvailableSlots(profileId, date);
        return ResponseEntity.ok(slots);
    }
}
