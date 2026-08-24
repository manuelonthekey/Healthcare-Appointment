package com.healthcare.appointment.repository;
import com.healthcare.appointment.model.DoctorAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DoctorAvailabilityRepository extends JpaRepository<DoctorAvailability, Long> {
    List<DoctorAvailability> findByDoctorProfileIdAndDayOfWeek(Long doctorProfileId, Integer dayOfWeek);
    List<DoctorAvailability> findByDoctorProfileId(Long doctorProfileId);
}
