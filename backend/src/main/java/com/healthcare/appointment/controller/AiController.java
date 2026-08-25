package com.healthcare.appointment.controller;

import com.healthcare.appointment.dto.ClinicalSummaryResponse;
import com.healthcare.appointment.dto.SymptomAnalysisResponse;
import com.healthcare.appointment.service.llm.LlmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private LlmService llmService;

    // Accessible by PATIENTS during booking flow
    @PostMapping("/analyze-symptoms")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    public ResponseEntity<SymptomAnalysisResponse> analyzeSymptoms(@RequestBody Map<String, String> payload) {
        String rawSymptoms = payload.get("symptoms");
        if (rawSymptoms == null || rawSymptoms.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        SymptomAnalysisResponse response = llmService.analyzeSymptoms(rawSymptoms);
        return ResponseEntity.ok(response);
    }

    // Accessible by DOCTORS during post-visit review
    @PostMapping("/summarize-notes")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ClinicalSummaryResponse> summarizeNotes(@RequestBody Map<String, String> payload) {
        String rawNotes = payload.get("notes");
        if (rawNotes == null || rawNotes.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        ClinicalSummaryResponse response = llmService.summarizeClinicalNotes(rawNotes);
        return ResponseEntity.ok(response);
    }
}
