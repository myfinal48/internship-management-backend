package com._projects.internship.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.MimeMessagePreparator;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.Session;
import java.io.InputStream;

@Configuration
@Profile("test")
public class TestMailConfig {
    @Bean
    public JavaMailSender javaMailSender() {
        return new JavaMailSender() {
            @Override
            public MimeMessage createMimeMessage() {
                return new MimeMessage((Session) null);
            }
            @Override
            public MimeMessage createMimeMessage(InputStream contentStream) {
                try {
                    return new MimeMessage((Session) null, contentStream);
                } catch (Exception e) {
                    return null;
                }
            }
            @Override
            public void send(MimeMessage mimeMessage) {
                // No-op in test profile: email sending is intentionally disabled
            }
            @Override
            public void send(MimeMessage... mimeMessages) {
                // No-op in test profile: email sending is intentionally disabled
            }
            @Override
            public void send(MimeMessagePreparator mimeMessagePreparator) {
                // No-op in test profile: email sending is intentionally disabled
            }
            @Override
            public void send(MimeMessagePreparator... mimeMessagePreparators) {
                // No-op in test profile: email sending is intentionally disabled
            }
            @Override
            public void send(SimpleMailMessage simpleMessage) {
                // No-op in test profile: email sending is intentionally disabled
            }
            @Override
            public void send(SimpleMailMessage... simpleMessages) {
                // No-op in test profile: email sending is intentionally disabled
            }
        };
    }
}