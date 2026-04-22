package com.fm.foodmanagementsystem.core.services.interfaces;

import jakarta.mail.MessagingException;
import java.util.Map;

public interface IEmailService {
    void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> templateModel) throws MessagingException;
}