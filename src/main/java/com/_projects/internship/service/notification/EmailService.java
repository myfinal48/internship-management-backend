package com._projects.internship.service.notification;

public interface EmailService {
    void sendEmail(String to, String subject, String body);
}
