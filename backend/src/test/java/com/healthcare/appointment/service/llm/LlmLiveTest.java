package com.healthcare.appointment.service.llm;

import com.healthcare.appointment.dto.ClinicalSummaryResponse;
import com.healthcare.appointment.dto.SymptomAnalysisResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.junit.jupiter.api.Disabled;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Disabled("Disabled for CI/CD as it requires a real API Key")
@org.springframework.boot.test.mock.mockito.MockBean(com.healthcare.appointment.service.MedicationReminderTask.class)
public class LlmLiveTest {

    @Autowired
    private LlmService llmService;

    @Test
    public void liveSymptomAnalysis_UsesOpenRouter() {
        System.out.println("\n--- RUNNING LIVE LLM TEST: PRE-VISIT SYMPTOMS ---");
        String symptoms = "Patient complains of severe chest pain, shortness of breath, and radiating pain in the left arm for the past 2 hours.";
        
        SymptomAnalysisResponse response = llmService.analyzeSymptoms(symptoms);
        
        System.out.println("Raw Input: " + symptoms);
        System.out.println("Chief Complaint: " + response.getChiefComplaint());
        System.out.println("Urgency Level: " + response.getUrgencyLevel());
        System.out.println("Extracted Symptoms: " + response.getExtractedSymptoms());
        System.out.println("Suggested Questions: " + response.getSuggestedQuestions());
        
        assertThat(response).isNotNull();
        // It shouldn't use the fallback
        assertThat(response.getUrgencyLevel()).isNotEqualTo("UNKNOWN");
        assertThat(response.getChiefComplaint()).isNotEqualTo("AI Analysis Unavailable");
    }

    @Test
    public void liveClinicalNotes_UsesOpenRouter() {
        System.out.println("\n--- RUNNING LIVE LLM TEST: POST-VISIT NOTES ---");
        String notes = "Pt is a 45yo male. C/O headaches. BP 120/80. Prescribed ibuprofen 400mg PRN. RTC in 2 weeks if not resolved.";
        
        ClinicalSummaryResponse response = llmService.summarizeClinicalNotes(notes);
        
        System.out.println("Raw Notes: " + notes);
        System.out.println("Structured Summary: " + response.getStructuredSummary());
        System.out.println("Key Takeaways: " + response.getKeyTakeaways());
        
        assertThat(response).isNotNull();
        // It shouldn't use the fallback
        assertThat(response.getStructuredSummary()).isNotEqualTo("AI Summarization Unavailable. Please refer to raw notes.");
    }
}
