package com.healthcare.appointment.controller;

import com.healthcare.appointment.dto.DoctorDtos.StatusUpdateRequest;
import com.healthcare.appointment.model.DoctorProfile;
import com.healthcare.appointment.repository.DoctorProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/doctors")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private DoctorProfileRepository profileRepository;

    @GetMapping
    public List<DoctorProfile> getAllDoctors(@RequestParam(required = false) String status) {
        if (status != null) {
            return profileRepository.findByStatus(status.toUpperCase());
        }
        return profileRepository.findAll();
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateDoctorStatus(@PathVariable Long id, @RequestBody StatusUpdateRequest request) {
        return profileRepository.findById(id).map(profile -> {
            profile.setStatus(request.status.toUpperCase());
            profileRepository.save(profile);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }

    public static class CreateDoctorRequest {
        public String email;
        public String password;
        public String name;
        public String specialization;
        public Integer slotDurationMins;
    }

    @Autowired private com.healthcare.appointment.repository.UserRepository userRepository;
    @Autowired private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @PostMapping
    public ResponseEntity<?> createDoctor(@RequestBody CreateDoctorRequest req) {
        if (userRepository.findByEmail(req.email).isPresent()) {
            return ResponseEntity.badRequest().body("Email already in use");
        }
        
        com.healthcare.appointment.model.User user = new com.healthcare.appointment.model.User();
        user.setEmail(req.email);
        user.setPasswordHash(passwordEncoder.encode(req.password));
        user.setRole("DOCTOR");
        user = userRepository.save(user);

        DoctorProfile profile = new DoctorProfile();
        profile.setUser(user);
        profile.setName(req.name);
        profile.setSpecialization(req.specialization);
        profile.setSlotDurationMins(req.slotDurationMins != null ? req.slotDurationMins : 30);
        profile.setStatus("ACTIVE"); // Initially active when created by admin
        profile = profileRepository.save(profile);
        
        return ResponseEntity.ok(profile);
    }

    @Autowired private com.healthcare.appointment.repository.DoctorAvailabilityRepository availabilityRepository;

    @org.springframework.transaction.annotation.Transactional
    @PutMapping("/{id}/working-hours")
    public ResponseEntity<?> setWorkingHours(@PathVariable Long id, @RequestBody java.util.List<com.healthcare.appointment.model.DoctorAvailability> availabilities) {
        if (!profileRepository.existsById(id)) return ResponseEntity.notFound().build();
        
        // Delete old and save new
        java.util.List<com.healthcare.appointment.model.DoctorAvailability> existing = availabilityRepository.findByDoctorProfileId(id);
        availabilityRepository.deleteAll(existing);
        
        for (com.healthcare.appointment.model.DoctorAvailability a : availabilities) {
            a.setDoctorProfileId(id);
        }
        availabilityRepository.saveAll(availabilities);
        return ResponseEntity.ok().build();
    }

    @Autowired private com.healthcare.appointment.repository.DoctorLeaveRepository leaveRepository;
    @Autowired private org.springframework.context.ApplicationEventPublisher eventPublisher;

    @PostMapping("/{id}/leaves")
    public ResponseEntity<?> addLeave(@PathVariable Long id, @RequestBody com.healthcare.appointment.model.DoctorLeave leave) {
        if (!profileRepository.existsById(id)) return ResponseEntity.notFound().build();
        
        leave.setDoctorProfileId(id);
        com.healthcare.appointment.model.DoctorLeave saved = leaveRepository.save(leave);
        eventPublisher.publishEvent(new com.healthcare.appointment.event.LeaveAddedEvent(this, id, leave.getLeaveDate()));
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}/leaves/{leaveId}")
    public ResponseEntity<?> removeLeave(@PathVariable Long id, @PathVariable Long leaveId) {
        leaveRepository.deleteById(leaveId);
        return ResponseEntity.ok().build();
    }
}
