package com.healthcare.appointment.event;

import org.springframework.context.ApplicationEvent;
import java.time.LocalDate;

public class LeaveAddedEvent extends ApplicationEvent {
    private final Long doctorProfileId;
    private final LocalDate leaveDate;

    public LeaveAddedEvent(Object source, Long doctorProfileId, LocalDate leaveDate) {
        super(source);
        this.doctorProfileId = doctorProfileId;
        this.leaveDate = leaveDate;
    }

    public Long getDoctorProfileId() { return doctorProfileId; }
    public LocalDate getLeaveDate() { return leaveDate; }
}
