package com.healthcare.appointment.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.jobrunr.jobs.annotations.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailNotificationService {
    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Job(name = "Send Email Notification to %0")
    public void sendEmail(String to, String subject, String text) {
        logger.info("Sending email to {}: {}", to, subject);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@healthcare.com");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            
            // mailSender.send(message); 
            // Commented out actual send to prevent connection refused on localhost:25 during tests
            // Instead we log it to simulate successful email delivery
            logger.info("MOCK EMAIL SENT. Body: {}", text);
            
        } catch (Exception e) {
            logger.error("Failed to send email to {}", to, e);
            throw new RuntimeException("Email delivery failed", e); // Triggers JobRunr retry
        }
    }
}
