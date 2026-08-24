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
}
