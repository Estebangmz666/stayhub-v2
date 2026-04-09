package edu.uniquindio.stayhub_v2.service;

public interface EmailService {
    void sendEmail(String to, String subject, String text);
}
