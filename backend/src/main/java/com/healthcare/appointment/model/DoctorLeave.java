package com.healthcare.appointment.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "doctor_leaves")
public class DoctorLeave {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "doctor_profile_id", nullable = false)
    private Long doctorProfileId;

    @Column(name = "leave_date", nullable = false)
    private LocalDate leaveDate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getDoctorProfileId() { return doctorProfileId; }
    public void setDoctorProfileId(Long doctorProfileId) { this.doctorProfileId = doctorProfileId; }
    public LocalDate getLeaveDate() { return leaveDate; }
    public void setLeaveDate(LocalDate leaveDate) { this.leaveDate = leaveDate; }
}
