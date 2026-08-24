package com.healthcare.appointment.repository;

import com.healthcare.appointment.model.Appointment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Appointment a WHERE a.doctorProfileId = :doctorId AND a.appointmentDatetime = :datetime")
    Optional<Appointment> findByDoctorAndDatetimeWithLock(
        @Param("doctorId") Long doctorId, 
        @Param("datetime") LocalDateTime datetime
    );
}
