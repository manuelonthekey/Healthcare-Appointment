package com.healthcare.appointment.dto;

import java.util.List;

public class ClinicalSummaryResponse {
    private String structuredSummary;
    private List<String> keyTakeaways;
    private String disclaimer = "AI generated summary. Must be reviewed by the attending physician.";

    // Getters and Setters
    public String getStructuredSummary() { return structuredSummary; }
    public void setStructuredSummary(String structuredSummary) { this.structuredSummary = structuredSummary; }
    public List<String> getKeyTakeaways() { return keyTakeaways; }
    public void setKeyTakeaways(List<String> keyTakeaways) { this.keyTakeaways = keyTakeaways; }
    public String getDisclaimer() { return disclaimer; }
    public void setDisclaimer(String disclaimer) { this.disclaimer = disclaimer; }
}
