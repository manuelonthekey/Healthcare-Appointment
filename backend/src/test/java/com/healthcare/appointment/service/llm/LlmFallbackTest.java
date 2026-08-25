package com.healthcare.appointment.service.llm;

import com.healthcare.appointment.dto.ClinicalSummaryResponse;
import com.healthcare.appointment.dto.SymptomAnalysisResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "llm.api.key=") // Force empty API key to test fallback
public class LlmFallbackTest {

    @Autowired
    private LlmService llmService;

    @Test
    public void fallbackSymptomAnalysis_TriggersWhenNoApiKey() {
        SymptomAnalysisResponse response = llmService.analyzeSymptoms("I have a severe headache and fever");
        
        assertThat(response).isNotNull();
        assertThat(response.getUrgencyLevel()).isEqualTo("UNKNOWN");
        assertThat(response.getChiefComplaint()).isEqualTo("AI Analysis Unavailable");
        assertThat(response.getDisclaimer()).contains("assistive and not a medical diagnosis");
    }

    @Test
    public void fallbackClinicalNotes_TriggersWhenNoApiKey() {
        ClinicalSummaryResponse response = llmService.summarizeClinicalNotes("Patient presented with 102F fever. Prescribed Paracetamol.");
        
        assertThat(response).isNotNull();
        assertThat(response.getStructuredSummary()).isEqualTo("AI Summarization Unavailable. Please refer to raw notes.");
        assertThat(response.getDisclaimer()).contains("Must be reviewed by the attending physician");
    }
}
