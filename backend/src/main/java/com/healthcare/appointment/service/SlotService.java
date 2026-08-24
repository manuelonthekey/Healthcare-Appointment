package com.healthcare.appointment.service;

import com.healthcare.appointment.model.Appointment;
import com.healthcare.appointment.model.DoctorAvailability;
import com.healthcare.appointment.model.DoctorProfile;
import com.healthcare.appointment.repository.AppointmentRepository;
import com.healthcare.appointment.repository.DoctorAvailabilityRepository;
import com.healthcare.appointment.repository.DoctorLeaveRepository;
import com.healthcare.appointment.repository.DoctorProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SlotService {
    @Autowired private DoctorProfileRepository doctorProfileRepository;
    @Autowired private DoctorAvailabilityRepository availabilityRepository;
    @Autowired private DoctorLeaveRepository leaveRepository;
    @Autowired private AppointmentRepository appointmentRepository;

    public List<LocalTime> generateAvailableSlots(Long doctorProfileId, LocalDate date) {
        DoctorProfile doc = doctorProfileRepository.findById(doctorProfileId)
                .orElseThrow(() -> new RuntimeException("Doctor profile not found"));
        
        if (!"ACTIVE".equals(doc.getStatus())) {
            return new ArrayList<>();
        }

        if (leaveRepository.existsByDoctorProfileIdAndLeaveDate(doctorProfileId, date)) {
            return new ArrayList<>(); // Full day leave
        }

        int dayOfWeek = date.getDayOfWeek().getValue();
        List<DoctorAvailability> availabilities = availabilityRepository.findByDoctorProfileIdAndDayOfWeek(doctorProfileId, dayOfWeek);
        
        if (availabilities.isEmpty()) {
            return new ArrayList<>();
        }

        int duration = doc.getSlotDurationMins();
        List<LocalTime> possibleSlots = new ArrayList<>();
        
        for (DoctorAvailability avail : availabilities) {
            LocalTime current = avail.getStartTime();
            while (current.plusMinutes(duration).isBefore(avail.getEndTime()) || current.plusMinutes(duration).equals(avail.getEndTime())) {
                possibleSlots.add(current);
                current = current.plusMinutes(duration);
            }
        }

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        List<Appointment> existingAppointments = appointmentRepository.findByDoctorProfileIdAndAppointmentDatetimeBetween(doctorProfileId, startOfDay, endOfDay);
        
        List<LocalTime> bookedTimes = existingAppointments.stream()
                .filter(a -> "SCHEDULED".equals(a.getStatus()) || 
                             ("HELD".equals(a.getStatus()) && (a.getExpiresAt() == null || a.getExpiresAt().isAfter(LocalDateTime.now()))))
                .map(a -> a.getAppointmentDatetime().toLocalTime())
                .collect(Collectors.toList());

        return possibleSlots.stream()
                .filter(slot -> !bookedTimes.contains(slot))
                .collect(Collectors.toList());
    }
}
