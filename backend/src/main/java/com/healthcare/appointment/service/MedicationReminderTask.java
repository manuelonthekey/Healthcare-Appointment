package com.healthcare.appointment.service;

import com.healthcare.appointment.model.MedicationReminder;
import com.healthcare.appointment.repository.MedicationReminderRepository;
import com.healthcare.appointment.model.PatientProfile;
import com.healthcare.appointment.repository.PatientProfileRepository;
import com.healthcare.appointment.model.User;
import com.healthcare.appointment.repository.UserRepository;
import org.jobrunr.scheduling.BackgroundJob;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class MedicationReminderTask {

    private final MedicationReminderRepository medicationReminderRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final UserRepository userRepository;
    private final EmailNotificationService emailNotificationService;

    public MedicationReminderTask(MedicationReminderRepository medicationReminderRepository,
                                  PatientProfileRepository patientProfileRepository,
                                  UserRepository userRepository,
                                  EmailNotificationService emailNotificationService) {
        this.medicationReminderRepository = medicationReminderRepository;
        this.patientProfileRepository = patientProfileRepository;
        this.userRepository = userRepository;
        this.emailNotificationService = emailNotificationService;
    }

    @PostConstruct
    public void init() {
        // Run every minute
        BackgroundJob.scheduleRecurrently("medication-reminder-job", "* * * * *", () -> this.processDueReminders());
    }

    public void processDueReminders() {
        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = LocalTime.now();
        LocalDateTime startOfDay = currentDate.atStartOfDay();

        List<MedicationReminder> dueReminders = medicationReminderRepository.findDueReminders(currentDate, currentTime, startOfDay);

        for (MedicationReminder reminder : dueReminders) {
            try {
                // Fetch Patient Profile and User
                PatientProfile profile = patientProfileRepository.findById(reminder.getPatientProfileId()).orElse(null);
                if (profile == null) continue;

                User user = userRepository.findById(profile.getUserId()).orElse(null);
                if (user == null || user.getEmail() == null) continue;

                // Send email
                String subject = "Medication Reminder: " + reminder.getMedicationName();
                String body = String.format(
                    "Medication Reminder\n\nMedication: %s\nDosage: %s\nScheduled time: %s\n\nPlease follow the medication instructions provided by your doctor.",
                    reminder.getMedicationName(),
                    reminder.getDosage() != null ? reminder.getDosage() : "As prescribed",
                    reminder.getReminderTime().toString()
                );

                emailNotificationService.sendEmail(user.getEmail(), subject, body);

                // Update lastNotifiedAt to avoid duplicate sends
                reminder.setLastNotifiedAt(LocalDateTime.now());
                medicationReminderRepository.save(reminder);
            } catch (Exception e) {
                System.err.println("Failed to process medication reminder " + reminder.getId() + ": " + e.getMessage());
            }
        }
    }
}
