package com.healthcare.appointment.repository;
import com.healthcare.appointment.model.DoctorLeave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface DoctorLeaveRepository extends JpaRepository<DoctorLeave, Long> {
    boolean existsByDoctorProfileIdAndLeaveDate(Long doctorProfileId, LocalDate leaveDate);
    List<DoctorLeave> findByDoctorProfileId(Long doctorProfileId);
}
