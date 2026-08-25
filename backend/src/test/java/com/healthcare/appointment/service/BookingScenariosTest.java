package com.healthcare.appointment.service;

import com.healthcare.appointment.model.Appointment;
import com.healthcare.appointment.model.DoctorProfile;
import com.healthcare.appointment.model.PatientProfile;
import com.healthcare.appointment.model.User;
import com.healthcare.appointment.repository.AppointmentRepository;
import com.healthcare.appointment.repository.DoctorProfileRepository;
import com.healthcare.appointment.repository.PatientProfileRepository;
import com.healthcare.appointment.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class BookingScenariosTest {

    @Autowired private PatientBookingService bookingService;
    @Autowired private SlotService slotService;
    @Autowired private AppointmentRepository appointmentRepository;
    @Autowired private DoctorProfileRepository doctorProfileRepository;
    @Autowired private PatientProfileRepository patientProfileRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private AppointmentCleanupTask cleanupTask;

    private Long docId;
    private Long pat1Id;
    private Long pat2Id;

    @BeforeEach
    public void setup() {
        // Clear previous runs
        appointmentRepository.deleteAll();
        patientProfileRepository.deleteAll();
        doctorProfileRepository.deleteAll();
        userRepository.deleteAll();

        // Setup User and Doctor
        User docUser = new User();
        docUser.setEmail("doc_scenarios_" + System.currentTimeMillis() + "@clinic.com");
        docUser.setPasswordHash("hash");
        docUser.setRole("DOCTOR");
        docUser = userRepository.save(docUser);

        DoctorProfile doc = new DoctorProfile();
        doc.setUser(docUser);
        doc.setName("Dr. Scenario");
        doc.setSpecialization("Testing");
        doc.setStatus("ACTIVE");
        doc.setSlotDurationMins(30);
        doc = doctorProfileRepository.save(doc);
        docId = doc.getId();

        // Setup Patient 1
        User p1User = new User();
        p1User.setEmail("pat1_scenarios_" + System.currentTimeMillis() + "@clinic.com");
        p1User.setPasswordHash("hash");
        p1User.setRole("PATIENT");
        p1User = userRepository.save(p1User);
        PatientProfile p1 = new PatientProfile();
        p1.setUserId(p1User.getId());
        p1.setName("Patient 1");
        p1 = patientProfileRepository.save(p1);
        pat1Id = p1.getId();

        // Setup Patient 2
        User p2User = new User();
        p2User.setEmail("pat2_scenarios_" + System.currentTimeMillis() + "@clinic.com");
        p2User.setPasswordHash("hash");
        p2User.setRole("PATIENT");
        p2User = userRepository.save(p2User);
        PatientProfile p2 = new PatientProfile();
        p2.setUserId(p2User.getId());
        p2.setName("Patient 2");
        p2 = patientProfileRepository.save(p2);
        pat2Id = p2.getId();
    }

    @Test
    public void scenario1_Cancellation() {
        System.out.println("\n--- RUNNING SCENARIO 1: APPOINTMENT CANCELLED ---");
        LocalDateTime slotTime = LocalDateTime.of(2030, 5, 5, 9, 0);
        
        // 1. Patient holds and confirms slot
        Appointment appt = bookingService.holdSlot(docId, pat1Id, slotTime, "Fever", null);
        appt = bookingService.confirmBooking(appt.getId(), pat1Id);
        System.out.println("1. Patient 1 booked slot at: " + appt.getAppointmentDatetime() + " | Status: " + appt.getStatus());

        // 2. Doctor or Patient cancels it
        appt.setStatus("CANCELLED");
        appointmentRepository.save(appt);
        System.out.println("2. Appointment was CANCELLED.");

        // 3. Verify it is freed up in the algorithm
        List<LocalTime> bookedTimes = appointmentRepository.findByDoctorProfileIdAndAppointmentDatetimeBetween(docId, slotTime.toLocalDate().atStartOfDay(), slotTime.toLocalDate().atTime(LocalTime.MAX))
                .stream().filter(a -> !"CANCELLED".equals(a.getStatus())).map(a -> a.getAppointmentDatetime().toLocalTime()).toList();
        
        System.out.println("3. Is the slot free for others? " + !bookedTimes.contains(slotTime.toLocalTime()));
        assertThat(bookedTimes).doesNotContain(slotTime.toLocalTime());
    }

    @Test
    public void scenario2_ConcurrentBooking() {
        System.out.println("\n--- RUNNING SCENARIO 2: CONCURRENT BOOKING ---");
        LocalDateTime slotTime = LocalDateTime.of(2030, 5, 5, 10, 0);

        System.out.println("1. Two patients attempting to hold the exact same slot concurrently...");
        CompletableFuture<Appointment> t1 = CompletableFuture.supplyAsync(() -> bookingService.holdSlot(docId, pat1Id, slotTime, "Fever", null));
        CompletableFuture<Appointment> t2 = CompletableFuture.supplyAsync(() -> bookingService.holdSlot(docId, pat2Id, slotTime, "Cough", null));
        
        CompletableFuture.allOf(t1, t2).handle((res, ex) -> null).join();

        if (t1.isCompletedExceptionally()) {
            System.out.println("2. Patient 1 FAILED (DataIntegrityViolationException)");
            System.out.println("2. Patient 2 SUCCEEDED");
        } else {
            System.out.println("2. Patient 1 SUCCEEDED");
            System.out.println("2. Patient 2 FAILED (DataIntegrityViolationException)");
        }
        
        assertThat(t1.isCompletedExceptionally() ^ t2.isCompletedExceptionally()).isTrue();
    }

    @Test
    public void scenario3_HoldExpires() {
        System.out.println("\n--- RUNNING SCENARIO 3: HOLD EXPIRES WITHOUT BOOKING ---");
        LocalDateTime slotTime = LocalDateTime.of(2030, 5, 5, 11, 0);

        // 1. Patient holds slot
        Appointment hold = bookingService.holdSlot(docId, pat1Id, slotTime, "Checkup", null);
        System.out.println("1. Patient holds slot. Status: " + hold.getStatus() + " | Expires At: " + hold.getExpiresAt());

        // 2. Simulate time passing (Force expiry)
        hold.setExpiresAt(LocalDateTime.now().minusMinutes(5));
        appointmentRepository.save(hold);
        System.out.println("2. Patient abandons session. 10 minutes pass. Hold is now EXPIRED.");

        // 3. Cron Job runs
        System.out.println("3. Background Cron Job executing...");
        cleanupTask.cleanupExpiredHolds();

        // 4. Verify slot is deleted
        boolean exists = appointmentRepository.findById(hold.getId()).isPresent();
        System.out.println("4. Does the abandoned hold still exist in DB? " + exists);
        assertThat(exists).isFalse();
    }

    @Autowired private org.springframework.context.ApplicationEventPublisher eventPublisher;

    @Test
    public void scenario4_LeaveConflict() throws InterruptedException {
        System.out.println("\n--- RUNNING SCENARIO 4: LEAVE CONFLICT BACKGROUND JOB ---");
        LocalDate leaveDate = LocalDate.of(2030, 8, 15);
        LocalDateTime slot1 = leaveDate.atTime(9, 0);
        LocalDateTime slot2 = leaveDate.atTime(10, 0);

        // 1. Two patients book appointments successfully
        Appointment a1 = bookingService.holdSlot(docId, pat1Id, slot1, "Fever", null);
        a1 = bookingService.confirmBooking(a1.getId(), pat1Id);
        
        Appointment a2 = bookingService.holdSlot(docId, pat2Id, slot2, "Cough", null);
        a2 = bookingService.confirmBooking(a2.getId(), pat2Id);

        System.out.println("1. Two patients booked slots on " + leaveDate);
        System.out.println("   - Appointment 1 Status: " + a1.getStatus());
        System.out.println("   - Appointment 2 Status: " + a2.getStatus());

        // 2. Doctor suddenly takes a leave
        System.out.println("2. Doctor submits an emergency leave for " + leaveDate + "...");
        com.healthcare.appointment.model.DoctorLeave leave = new com.healthcare.appointment.model.DoctorLeave();
        leave.setDoctorProfileId(docId);
        leave.setLeaveDate(leaveDate);
        
        System.out.println("3. Asynchronous @Async background job intercepts the Leave Added Event...");
        eventPublisher.publishEvent(new com.healthcare.appointment.event.LeaveAddedEvent(this, docId, leaveDate));
        
        // Let the async thread finish
        Thread.sleep(1000);

        // 4. Verify DB
        Appointment fetchedA1 = appointmentRepository.findById(a1.getId()).get();
        Appointment fetchedA2 = appointmentRepository.findById(a2.getId()).get();

        System.out.println("4. Checking database post-background job...");
        System.out.println("   - Appointment 1 Status: " + fetchedA1.getStatus());
        System.out.println("   - Appointment 2 Status: " + fetchedA2.getStatus());
        
        assertThat(fetchedA1.getStatus()).isEqualTo("CANCELLED");
        assertThat(fetchedA2.getStatus()).isEqualTo("CANCELLED");
    }
}
