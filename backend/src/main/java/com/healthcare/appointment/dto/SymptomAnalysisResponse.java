package com.healthcare.appointment.dto;

import java.util.List;

public class SymptomAnalysisResponse {
    private String chiefComplaint;
    private List<String> extractedSymptoms;
    private String urgencyLevel; // LOW, MEDIUM, HIGH, UNKNOWN
    private List<String> suggestedQuestions;
    private String disclaimer = "AI output is assistive and not a medical diagnosis.";

    // Getters and Setters
    public String getChiefComplaint() { return chiefComplaint; }
    public void setChiefComplaint(String chiefComplaint) { this.chiefComplaint = chiefComplaint; }
    public List<String> getExtractedSymptoms() { return extractedSymptoms; }
    public void setExtractedSymptoms(List<String> extractedSymptoms) { this.extractedSymptoms = extractedSymptoms; }
    public String getUrgencyLevel() { return urgencyLevel; }
    public void setUrgencyLevel(String urgencyLevel) { this.urgencyLevel = urgencyLevel; }
    public List<String> getSuggestedQuestions() { return suggestedQuestions; }
    public void setSuggestedQuestions(List<String> suggestedQuestions) { this.suggestedQuestions = suggestedQuestions; }
    public String getDisclaimer() { return disclaimer; }
    public void setDisclaimer(String disclaimer) { this.disclaimer = disclaimer; }
}
