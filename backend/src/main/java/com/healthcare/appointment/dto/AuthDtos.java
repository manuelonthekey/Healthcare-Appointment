package com.healthcare.appointment.dto;

public class AuthDtos {
    public static class LoginRequest {
        public String email;
        public String password;
    }
    public static class RegisterRequest {
        public String email;
        public String password;
        public String role; // "PATIENT", "DOCTOR", "ADMIN"
    }
    public static class AuthResponse {
        public String token;
        public String email;
        public String role;
        public Long id;
        public AuthResponse(String token, String email, String role, Long id) {
            this.token = token; this.email = email; this.role = role; this.id = id;
        }
    }
    public static class MessageResponse {
        public String message;
        public MessageResponse(String message) { this.message = message; }
    }
}
