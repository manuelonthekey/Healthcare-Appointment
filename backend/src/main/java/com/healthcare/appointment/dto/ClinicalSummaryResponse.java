package com.healthcare.appointment.dto;

import java.util.List;

public class ClinicalSummaryResponse {
    private String structuredSummary;
    private List<String> keyTakeaways;
    private List<MedicationDto> medications;
    private String disclaimer = "AI generated summary. Must be reviewed by the attending physician.";

    public static class MedicationDto {
        private String name;
        private String dosage;
        private String frequency;
        private List<String> times;
        private String startDate;
        private String endDate;
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDosage() { return dosage; }
        public void setDosage(String dosage) { this.dosage = dosage; }
        public String getFrequency() { return frequency; }
        public void setFrequency(String frequency) { this.frequency = frequency; }
        public List<String> getTimes() { return times; }
        public void setTimes(List<String> times) { this.times = times; }
        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }
        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }
    }

    // Getters and Setters
    public String getStructuredSummary() { return structuredSummary; }
    public void setStructuredSummary(String structuredSummary) { this.structuredSummary = structuredSummary; }
    public List<String> getKeyTakeaways() { return keyTakeaways; }
    public void setKeyTakeaways(List<String> keyTakeaways) { this.keyTakeaways = keyTakeaways; }
    public List<MedicationDto> getMedications() { return medications; }
    public void setMedications(List<MedicationDto> medications) { this.medications = medications; }
    public String getDisclaimer() { return disclaimer; }
    public void setDisclaimer(String disclaimer) { this.disclaimer = disclaimer; }
}
