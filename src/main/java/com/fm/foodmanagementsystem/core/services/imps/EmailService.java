package com.fm.foodmanagementsystem.core.services.imps;

import com.fm.foodmanagementsystem.core.services.interfaces.IEmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailService implements IEmailService {

    JavaMailSender javaMailSender;
    TemplateEngine templateEngine;

    @Override
    @Async // Đánh dấu hàm này chạy ngầm (Background thread) để API không bị lag chờ gửi mail
    public void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> templateModel) throws MessagingException {
        // Nhét biến (OTP, Email...) vào context của Thymeleaf
        Context thymeleafContext = new Context();
        thymeleafContext.setVariables(templateModel);

        // Render file HTML thành một chuỗi String
        String htmlBody = templateEngine.process(templateName, thymeleafContext);

        // Tạo thư MimeMessage để hỗ trợ định dạng HTML
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true); // true = Bật chế độ HTML

        javaMailSender.send(message);
        log.info("Đã gửi email thành công tới: {}", to);
    }
}