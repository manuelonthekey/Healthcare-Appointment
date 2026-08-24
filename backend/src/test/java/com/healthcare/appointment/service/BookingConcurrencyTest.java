package com.healthcare.appointment.service;

import com.healthcare.appointment.model.Appointment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class BookingConcurrencyTest {

    @Autowired private PatientBookingService bookingService;
    @Autowired private com.healthcare.appointment.repository.DoctorProfileRepository doctorProfileRepository;
    @Autowired private com.healthcare.appointment.repository.PatientProfileRepository patientProfileRepository;
    @Autowired private com.healthcare.appointment.repository.UserRepository userRepository;

    @Test
    public void testConcurrentSlotHold_OnlyOneSucceeds() {
        // Setup DB relationships to satisfy Foreign Key constraints
        com.healthcare.appointment.model.User docUser = new com.healthcare.appointment.model.User();
        docUser.setEmail("doctest@test.com");
        docUser.setPasswordHash("pass");
        docUser.setRole("DOCTOR");
        docUser = userRepository.save(docUser);

        com.healthcare.appointment.model.DoctorProfile doc = new com.healthcare.appointment.model.DoctorProfile();
        doc.setUser(docUser);
        doc.setName("Dr. Test");
        doc.setSpecialization("General");
        doc = doctorProfileRepository.save(doc);

        com.healthcare.appointment.model.User patUser = new com.healthcare.appointment.model.User();
        patUser.setEmail("pattest@test.com");
        patUser.setPasswordHash("pass");
        patUser.setRole("PATIENT");
        patUser = userRepository.save(patUser);

        com.healthcare.appointment.model.PatientProfile p1 = new com.healthcare.appointment.model.PatientProfile();
        p1.setUserId(patUser.getId());
        p1.setName("Patient 1");
        p1 = patientProfileRepository.save(p1);

        com.healthcare.appointment.model.PatientProfile p2 = new com.healthcare.appointment.model.PatientProfile();
        p2.setUserId(patUser.getId());
        p2.setName("Patient 2");
        p2 = patientProfileRepository.save(p2);

        final Long docId = doc.getId();
        final Long pat1Id = p1.getId();
        final Long pat2Id = p2.getId();
        LocalDateTime datetime = LocalDateTime.of(2030, 1, 1, 10, 0);

        CompletableFuture<Appointment> t1 = CompletableFuture.supplyAsync(() -> 
            bookingService.holdSlot(docId, pat1Id, datetime, "Fever")
        );
        CompletableFuture<Appointment> t2 = CompletableFuture.supplyAsync(() -> 
            bookingService.holdSlot(docId, pat2Id, datetime, "Cough")
        );

        CompletableFuture.allOf(t1, t2).handle((res, ex) -> null).join();
        
        boolean t1Failed = t1.isCompletedExceptionally();
        boolean t2Failed = t2.isCompletedExceptionally();

        // Exactly one thread should fail because of the DB Unique Constraint 
        // uq_doctor_appointment(doctor_profile_id, appointment_datetime)
        assertThat(t1Failed ^ t2Failed).isTrue();
    }
}
