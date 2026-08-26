package com.healthcare.appointment.repository;

import com.healthcare.appointment.model.MedicationReminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface MedicationReminderRepository extends JpaRepository<MedicationReminder, Long> {

    List<MedicationReminder> findByPatientProfileId(Long patientProfileId);

    // Find active reminders that are currently within their date range,
    // and where the reminder time is exactly the current hour/minute (since cron runs often).
    // Or just find those where reminder_time <= currentTime and last_notified_at is null or not today.
    @Query("SELECT m FROM MedicationReminder m WHERE m.active = true " +
           "AND m.startDate <= :currentDate AND m.endDate >= :currentDate " +
           "AND m.reminderTime <= :currentTime " +
           "AND (m.lastNotifiedAt IS NULL OR m.lastNotifiedAt < :startOfDay)")
    List<MedicationReminder> findDueReminders(
            @Param("currentDate") LocalDate currentDate, 
            @Param("currentTime") LocalTime currentTime,
            @Param("startOfDay") java.time.LocalDateTime startOfDay);

    boolean existsByAppointmentIdAndMedicationNameAndReminderTime(Long appointmentId, String medicationName, LocalTime reminderTime);
}
