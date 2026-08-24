package com.healthcare.appointment.service;

import com.healthcare.appointment.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AppointmentCleanupTask {

    @Autowired
    private AppointmentRepository appointmentRepository;

    // Runs every 60 seconds
    @Scheduled(fixedRate = 60000)
    public void cleanupExpiredHolds() {
        int deleted = appointmentRepository.deleteExpiredHolds(LocalDateTime.now());
        if (deleted > 0) {
            System.out.println("Background Job: Automatically cleared " + deleted + " expired slot holds.");
        }
    }
}
