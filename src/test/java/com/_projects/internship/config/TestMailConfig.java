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
import java.util.Properties;

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
            public void send(MimeMessage mimeMessage) {}
            @Override
            public void send(MimeMessage... mimeMessages) {}
            @Override
            public void send(MimeMessagePreparator mimeMessagePreparator) {}
            @Override
            public void send(MimeMessagePreparator... mimeMessagePreparators) {}
            @Override
            public void send(SimpleMailMessage simpleMessage) {}
            @Override
            public void send(SimpleMailMessage... simpleMessages) {}
        };
    }
} 