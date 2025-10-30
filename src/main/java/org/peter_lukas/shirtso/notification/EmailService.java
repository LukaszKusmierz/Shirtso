package org.peter_lukas.shirtso.notification;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

    private final Resend resendClient;
    private final String fromEmail;
    private final boolean emailEnabled;

    public EmailService(@Value("${app.notification.email.resend.api-key}") String resendApiKey,
                        @Value("${app.notification.email.from}") String fromEmail,
                        @Value("${app.notification.email.enabled:false}") boolean emailEnabled) {
        this.resendClient = new Resend(resendApiKey);
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
            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(to)
                    .subject(subject)
                    .text(text)
                    .build();

            CreateEmailResponse response = resendClient.emails().send(params);
            log.info("Email sent successfully to: {} with ID: {}", to, response.getId());
        } catch (ResendException e) {
            log.error("Failed to send email to: {}", to, e);
            throw new RuntimeException("Email sending failed: " + e.getMessage(), e);
        }
    }

//    /**
//     * Send HTML email (optional - for future use)
//     */
//    public void sendHtmlEmail(String to, String subject, String htmlContent) {
//        if (!emailEnabled) {
//            log.info("Email sending is disabled. Would have sent HTML email to: {}, subject: {}", to, subject);
//            log.debug("Email content: {}", htmlContent);
//            return;
//        }
//
//        try {
//            CreateEmailOptions params = CreateEmailOptions.builder()
//                    .from(fromEmail)
//                    .to(to)
//                    .subject(subject)
//                    .html(htmlContent)
//                    .build();
//
//            CreateEmailResponse response = resendClient.emails().send(params);
//            log.info("HTML email sent successfully to: {} with ID: {}", to, response.getId());
//        } catch (ResendException e) {
//            log.error("Failed to send HTML email to: {}", to, e);
//            throw new RuntimeException("HTML email sending failed: " + e.getMessage(), e);
//        }
//    }
}