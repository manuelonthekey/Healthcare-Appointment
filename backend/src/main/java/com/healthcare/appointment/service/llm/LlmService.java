package com.healthcare.appointment.service.llm;

import com.healthcare.appointment.dto.ClinicalSummaryResponse;
import com.healthcare.appointment.dto.SymptomAnalysisResponse;

public interface LlmService {
    SymptomAnalysisResponse analyzeSymptoms(String rawSymptoms);
    ClinicalSummaryResponse summarizeClinicalNotes(String rawNotes);
}
