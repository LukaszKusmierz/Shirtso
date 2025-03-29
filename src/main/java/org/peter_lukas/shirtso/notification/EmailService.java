package org.peter_lukas.shirtso.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String fromEmail;
    private final boolean emailEnabled;

    public EmailService(JavaMailSender mailSender,
                        @Value("${app.notification.email.from}") String fromEmail,
                        @Value("${app.notification.email.enabled:false}") boolean emailEnabled) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
        this.emailEnabled = emailEnabled;
    }

    public void sendEmail(String to, String subject, String text) {
        if (!emailEnabled) {
            log.info("Email sending is disabled. Would have sent email to: {}, subject: {}", to, subject);
            log.debug("Email content: {}", text);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
        }
    }
}
