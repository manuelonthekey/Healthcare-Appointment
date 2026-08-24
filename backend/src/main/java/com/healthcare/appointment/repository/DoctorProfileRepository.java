package com.healthcare.appointment.repository;
import com.healthcare.appointment.model.DoctorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, Long> {
    List<DoctorProfile> findByStatus(String status);
    Optional<DoctorProfile> findByUserId(Long userId);
}
