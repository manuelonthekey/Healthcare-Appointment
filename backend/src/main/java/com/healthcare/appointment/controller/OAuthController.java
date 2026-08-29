package com.healthcare.appointment.controller;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.healthcare.appointment.model.User;
import com.healthcare.appointment.repository.UserRepository;
import com.healthcare.appointment.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/calendar")
@CrossOrigin(origins = "*", maxAge = 3600)
public class OAuthController {

    @Value("${google.client.id}")
    private String clientId;

    @Value("${google.client.secret}")
    private String clientSecret;

    @Autowired
    private UserRepository userRepository;

    private static final String REDIRECT_URI = "http://localhost:8081/api/calendar/callback";

    @GetMapping("/connect")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> getGoogleAuthUrl() {
        if (clientId == null || clientId.equals("dummy-id")) {
            return ResponseEntity.badRequest().body(Map.of("message", "Google Client ID not configured."));
        }

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
            new NetHttpTransport(), GsonFactory.getDefaultInstance(),
            clientId, clientSecret,
            Collections.singletonList("https://www.googleapis.com/auth/calendar")
        ).setAccessType("offline").setApprovalPrompt("force").build();

        String url = flow.newAuthorizationUrl().setRedirectUri(REDIRECT_URI).build();
        return ResponseEntity.ok(Map.of("url", url));
    }

    @PostMapping("/callback")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> handleCallback(@RequestBody Map<String, String> body, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        String code = body.get("code");
        if (code == null || code.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Authorization code is missing."));
        }
        
        try {
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                new NetHttpTransport(), GsonFactory.getDefaultInstance(),
                clientId, clientSecret,
                Collections.singletonList("https://www.googleapis.com/auth/calendar")
            ).setAccessType("offline").build();

            GoogleTokenResponse response = flow.newTokenRequest(code).setRedirectUri(REDIRECT_URI).execute();
            String refreshToken = response.getRefreshToken();
            
            if (refreshToken != null) {
                User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
                user.setRefreshToken(refreshToken);
                userRepository.save(user);
                return ResponseEntity.ok(Map.of("message", "Calendar connected successfully"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("message", "No refresh token received. You may need to disconnect and reconnect to force approval."));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Failed to exchange token", "error", e.getMessage()));
        }
    }
}
