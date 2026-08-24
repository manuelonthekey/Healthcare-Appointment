package com.healthcare.appointment.service;

import com.healthcare.appointment.model.Appointment;
import com.healthcare.appointment.model.DoctorAvailability;
import com.healthcare.appointment.model.DoctorProfile;
import com.healthcare.appointment.repository.AppointmentRepository;
import com.healthcare.appointment.repository.DoctorAvailabilityRepository;
import com.healthcare.appointment.repository.DoctorLeaveRepository;
import com.healthcare.appointment.repository.DoctorProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SlotServiceTest {

    @InjectMocks
    private SlotService slotService;

    @Mock private DoctorProfileRepository doctorProfileRepository;
    @Mock private DoctorAvailabilityRepository availabilityRepository;
    @Mock private DoctorLeaveRepository leaveRepository;
    @Mock private AppointmentRepository appointmentRepository;

    @Test
    public void testGenerateAvailableSlots_Success() {
        Long docId = 1L;
        LocalDate testDate = LocalDate.of(2026, 8, 24); // Monday
        
        DoctorProfile doc = new DoctorProfile();
        doc.setStatus("ACTIVE");
        doc.setSlotDurationMins(30);

        DoctorAvailability avail = new DoctorAvailability();
        avail.setStartTime(LocalTime.of(9, 0));
        avail.setEndTime(LocalTime.of(11, 0)); // 4 possible slots: 9:00, 9:30, 10:00, 10:30

        Appointment existingAppt = new Appointment();
        existingAppt.setAppointmentDatetime(LocalDateTime.of(testDate, LocalTime.of(9, 30)));
        existingAppt.setStatus("HELD");

        when(doctorProfileRepository.findById(docId)).thenReturn(Optional.of(doc));
        when(leaveRepository.existsByDoctorProfileIdAndLeaveDate(docId, testDate)).thenReturn(false);
        when(availabilityRepository.findByDoctorProfileIdAndDayOfWeek(docId, 1)).thenReturn(List.of(avail));
        when(appointmentRepository.findByDoctorProfileIdAndAppointmentDatetimeBetween(eq(docId), any(), any()))
                .thenReturn(List.of(existingAppt));

        List<LocalTime> slots = slotService.generateAvailableSlots(docId, testDate);
        
        // Assert Algorithm works accurately!
        assertThat(slots).hasSize(3);
        assertThat(slots).containsExactly(
            LocalTime.of(9, 0),
            LocalTime.of(10, 0),
            LocalTime.of(10, 30)
        );
    }

    @Test
    public void testGenerateAvailableSlots_OnLeave() {
        Long docId = 1L;
        LocalDate testDate = LocalDate.of(2026, 8, 24);
        DoctorProfile doc = new DoctorProfile();
        doc.setStatus("ACTIVE");

        when(doctorProfileRepository.findById(docId)).thenReturn(Optional.of(doc));
        when(leaveRepository.existsByDoctorProfileIdAndLeaveDate(docId, testDate)).thenReturn(true);

        List<LocalTime> slots = slotService.generateAvailableSlots(docId, testDate);
        assertThat(slots).isEmpty();
    }
}
