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

    @org.springframework.data.jpa.repository.Query("SELECT d FROM DoctorProfile d WHERE d.status = 'ACTIVE' " +
        "AND (:name IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
        "AND (:specialty IS NULL OR LOWER(d.specialization) LIKE LOWER(CONCAT('%', :specialty, '%')))")
    List<DoctorProfile> searchDoctors(
        @org.springframework.data.repository.query.Param("name") String name, 
        @org.springframework.data.repository.query.Param("specialty") String specialty
    );
}
