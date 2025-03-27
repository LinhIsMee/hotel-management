package com.spring3.hotel.management.services;

public interface EmailService {
    void sendEmail(String to, String subject, String text);
} 