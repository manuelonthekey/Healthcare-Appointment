package com.healthcare.appointment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.appointment.controller.AppointmentController.CompleteRequest;
import com.healthcare.appointment.dto.ClinicalSummaryResponse;
import com.healthcare.appointment.model.Appointment;
import com.healthcare.appointment.model.DoctorProfile;
import com.healthcare.appointment.model.MedicationReminder;
import com.healthcare.appointment.model.PatientProfile;
import com.healthcare.appointment.model.User;
import com.healthcare.appointment.repository.AppointmentRepository;
import com.healthcare.appointment.repository.DoctorProfileRepository;
import com.healthcare.appointment.repository.MedicationReminderRepository;
import com.healthcare.appointment.repository.PatientProfileRepository;
import com.healthcare.appointment.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@org.springframework.boot.test.mock.mockito.MockBean(com.healthcare.appointment.service.MedicationReminderTask.class)
public class MedicationScenariosTest {

    @Autowired private TestRestTemplate restTemplate;
    @Autowired private AppointmentRepository appointmentRepository;
    @Autowired private DoctorProfileRepository doctorProfileRepository;
    @Autowired private PatientProfileRepository patientProfileRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private MedicationReminderRepository medicationReminderRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private ObjectMapper objectMapper;

    private Long docId;
    private Long pat1Id;
    private Long pat2Id;
    private Long appt1Id;
    
    private String doctorToken;
    private String pat1Token;
    private String pat2Token;

    @BeforeEach
    public void setup() {
        medicationReminderRepository.deleteAll();
        appointmentRepository.deleteAll();
        patientProfileRepository.deleteAll();
        doctorProfileRepository.deleteAll();
        userRepository.deleteAll();

        // Doctor
        User docUser = new User();
        docUser.setEmail("doc@med.com");
        docUser.setPasswordHash(passwordEncoder.encode("doc123"));
        docUser.setRole("DOCTOR");
        docUser = userRepository.save(docUser);

        DoctorProfile docProfile = new DoctorProfile();
        docProfile.setUser(docUser);
        docProfile.setName("Dr. Med");
        docProfile.setSpecialization("General");
        docProfile.setStatus("ACTIVE");
        docProfile = doctorProfileRepository.save(docProfile);
        docId = docProfile.getId();

        // Patient 1
        User pat1User = new User();
        pat1User.setEmail("pat1@med.com");
        pat1User.setPasswordHash(passwordEncoder.encode("pat123"));
        pat1User.setRole("PATIENT");
        pat1User = userRepository.save(pat1User);

        PatientProfile pat1Profile = new PatientProfile();
        pat1Profile.setUserId(pat1User.getId());
        pat1Profile.setName("Pat One");
        pat1Profile = patientProfileRepository.save(pat1Profile);
        pat1Id = pat1Profile.getId();
        
        // Patient 2
        User pat2User = new User();
        pat2User.setEmail("pat2@med.com");
        pat2User.setPasswordHash(passwordEncoder.encode("pat223"));
        pat2User.setRole("PATIENT");
        pat2User = userRepository.save(pat2User);

        PatientProfile pat2Profile = new PatientProfile();
        pat2Profile.setUserId(pat2User.getId());
        pat2Profile.setName("Pat Two");
        pat2Profile = patientProfileRepository.save(pat2Profile);
        pat2Id = pat2Profile.getId();

        // Appt
        Appointment appt = new Appointment();
        appt.setDoctorProfileId(docId);
        appt.setPatientProfileId(pat1Id);
        appt.setAppointmentDatetime(LocalDateTime.now().plusDays(1));
        appt.setStatus("SCHEDULED");
        appt = appointmentRepository.save(appt);
        appt1Id = appt.getId();
        
        doctorToken = login("doc@med.com", "doc123");
        pat1Token = login("pat1@med.com", "pat123");
        pat2Token = login("pat2@med.com", "pat223");
    }

    private String login(String email, String password) {
        Map<String, String> creds = Map.of("email", email, "password", password);
        ResponseEntity<Map> res = restTemplate.postForEntity("/api/auth/login", creds, Map.class);
        return (String) res.getBody().get("token");
    }

    private HttpHeaders headers(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }

    @Test
    public void testLlmParsing_AndNoReminderRowsCreatedIfEmpty() {
        // LLM parsing - without medication
        String rawJson = "{\"structuredSummary\": \"No meds\", \"keyTakeaways\": [], \"medications\": []}";
        try {
            ClinicalSummaryResponse response = objectMapper.readValue(rawJson, ClinicalSummaryResponse.class);
            assertThat(response.getMedications()).isEmpty();
            
            // Send to complete endpoint
            CompleteRequest req = new CompleteRequest();
            req.clinicalNotes = "No meds";
            req.aiSummary = "Summary";
            req.medications = response.getMedications();
            
            ResponseEntity<String> res = restTemplate.exchange("/api/appointments/" + appt1Id + "/complete", HttpMethod.POST, new HttpEntity<>(req, headers(doctorToken)), String.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
            
            assertThat(medicationReminderRepository.findAll()).isEmpty();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testPersistence_AndDuplicateProtection() {
        CompleteRequest req = new CompleteRequest();
        req.clinicalNotes = "Take Aspirin";
        req.aiSummary = "Summary";
        ClinicalSummaryResponse.MedicationDto med = new ClinicalSummaryResponse.MedicationDto();
        med.setName("Aspirin");
        med.setDosage("10mg");
        med.setFrequency("Daily");
        med.setTimes(Arrays.asList("09:00", "21:00"));
        med.setStartDate(LocalDate.now().toString());
        med.setEndDate(LocalDate.now().plusDays(5).toString());
        req.medications = List.of(med);
        
        // Complete appointment
        ResponseEntity<String> res = restTemplate.exchange("/api/appointments/" + appt1Id + "/complete", HttpMethod.POST, new HttpEntity<>(req, headers(doctorToken)), String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        List<MedicationReminder> reminders = medicationReminderRepository.findAll();
        assertThat(reminders).hasSize(2); // one for 09:00, one for 21:00
        assertThat(reminders.get(0).getPatientProfileId()).isEqualTo(pat1Id);
        assertThat(reminders.get(0).getAppointmentId()).isEqualTo(appt1Id);
        
        // Try complete again with duplicate payload (change status back to scheduled to allow call)
        Appointment a = appointmentRepository.findById(appt1Id).get();
        a.setStatus("SCHEDULED");
        appointmentRepository.save(a);
        
        res = restTemplate.exchange("/api/appointments/" + appt1Id + "/complete", HttpMethod.POST, new HttpEntity<>(req, headers(doctorToken)), String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        // Still 2 due to idempotency!
        assertThat(medicationReminderRepository.findAll()).hasSize(2);
    }
    
    @Test
    public void testSecurity_PatientCannotRetrieveAnotherPatientsMedications() {
        // Create reminder for pat1
        MedicationReminder r = new MedicationReminder();
        r.setPatientProfileId(pat1Id);
        r.setAppointmentId(appt1Id);
        r.setMedicationName("Aspirin");
        r.setReminderTime(LocalTime.of(9, 0));
        r.setStartDate(LocalDate.now());
        r.setEndDate(LocalDate.now().plusDays(5));
        medicationReminderRepository.save(r);
        
        // Pat 1 fetches
        ResponseEntity<List> res1 = restTemplate.exchange("/api/patients/medications", HttpMethod.GET, new HttpEntity<>(headers(pat1Token)), List.class);
        assertThat(res1.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res1.getBody()).hasSize(1);
        
        // Pat 2 fetches
        ResponseEntity<List> res2 = restTemplate.exchange("/api/patients/medications", HttpMethod.GET, new HttpEntity<>(headers(pat2Token)), List.class);
        assertThat(res2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res2.getBody()).isEmpty();
    }

    @Test
    public void testSchedulerQuery_FiltersCorrectlyAndDuplicateProtection() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.of(12, 0); // Hardcode to noon to prevent flaky midnight failures

        // 1. Due Reminder (valid dates, past time, not notified today)
        MedicationReminder due = new MedicationReminder();
        due.setAppointmentId(appt1Id);
        due.setPatientProfileId(pat1Id);
        due.setMedicationName("Due Med");
        due.setReminderTime(now.minusMinutes(10));
        due.setStartDate(today.minusDays(1));
        due.setEndDate(today.plusDays(1));
        medicationReminderRepository.save(due);

        // 2. Future-dated reminder (starts tomorrow)
        MedicationReminder future = new MedicationReminder();
        future.setAppointmentId(appt1Id);
        future.setPatientProfileId(pat1Id);
        future.setMedicationName("Future Med");
        future.setReminderTime(now.minusMinutes(10));
        future.setStartDate(today.plusDays(1));
        future.setEndDate(today.plusDays(3));
        medicationReminderRepository.save(future);

        // 3. Expired reminder (ended yesterday)
        MedicationReminder expired = new MedicationReminder();
        expired.setAppointmentId(appt1Id);
        expired.setPatientProfileId(pat1Id);
        expired.setMedicationName("Expired Med");
        expired.setReminderTime(now.minusMinutes(10));
        expired.setStartDate(today.minusDays(5));
        expired.setEndDate(today.minusDays(1));
        medicationReminderRepository.save(expired);
        
        // 4. Duplicate protection (already notified today)
        MedicationReminder notified = new MedicationReminder();
        notified.setAppointmentId(appt1Id);
        notified.setPatientProfileId(pat1Id);
        notified.setMedicationName("Notified Med");
        notified.setReminderTime(now.minusMinutes(10));
        notified.setStartDate(today.minusDays(1));
        notified.setEndDate(today.plusDays(1));
        notified.setLastNotifiedAt(LocalDateTime.now().minusMinutes(5));
        medicationReminderRepository.save(notified);

        List<MedicationReminder> results = medicationReminderRepository.findDueReminders(
                today, now, today.atStartOfDay());

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getMedicationName()).isEqualTo("Due Med");
    }
}
