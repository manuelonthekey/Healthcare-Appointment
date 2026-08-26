package com.healthcare.appointment.service;

import com.healthcare.appointment.model.MedicationReminder;
import com.healthcare.appointment.model.PatientProfile;
import com.healthcare.appointment.model.User;
import com.healthcare.appointment.repository.MedicationReminderRepository;
import com.healthcare.appointment.repository.PatientProfileRepository;
import com.healthcare.appointment.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class MedicationReminderTaskTest {

    @Mock private MedicationReminderRepository medicationReminderRepository;
    @Mock private PatientProfileRepository patientProfileRepository;
    @Mock private UserRepository userRepository;
    @Mock private EmailNotificationService emailNotificationService;

    @InjectMocks
    private MedicationReminderTask task;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testProcessReminders_DueReminderSendsEmail_FutureAndExpiredDoNot() {
        // We will simulate what the repository query returns.
        // The repository query (tested implicitly by JPA) returns due reminders.
        // We will mock the repository to return 1 due reminder.
        
        MedicationReminder dueReminder = new MedicationReminder();
        dueReminder.setId(1L);
        dueReminder.setPatientProfileId(10L);
        dueReminder.setMedicationName("Aspirin");
        dueReminder.setDosage("10mg");
        dueReminder.setReminderTime(LocalTime.now().minusMinutes(5));
        
        when(medicationReminderRepository.findDueReminders(any(LocalDate.class), any(LocalTime.class), any(LocalDateTime.class)))
            .thenReturn(Arrays.asList(dueReminder));
            
        PatientProfile p = new PatientProfile();
        p.setUserId(20L);
        when(patientProfileRepository.findById(10L)).thenReturn(Optional.of(p));
        
        User u = new User();
        u.setEmail("test@test.com");
        when(userRepository.findById(20L)).thenReturn(Optional.of(u));
        
        // Run process
        ReflectionTestUtils.invokeMethod(task, "processDueReminders");
        
        // Verify email sent
        verify(emailNotificationService, times(1)).sendEmail(eq("test@test.com"), anyString(), anyString());
        
        // Verify lastNotifiedAt updated and saved
        ArgumentCaptor<MedicationReminder> captor = ArgumentCaptor.forClass(MedicationReminder.class);
        verify(medicationReminderRepository, times(1)).save(captor.capture());
        
        assertThat(captor.getValue().getLastNotifiedAt()).isNotNull();
    }
}
