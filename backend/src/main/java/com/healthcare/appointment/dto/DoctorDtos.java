package com.healthcare.appointment.dto;
public class DoctorDtos {
    public static class ProfileRequest {
        public String name;
        public String specialization;
        public Integer slotDurationMins;
    }
    public static class StatusUpdateRequest {
        public String status;
    }
}
