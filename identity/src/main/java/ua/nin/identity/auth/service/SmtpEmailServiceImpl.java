package ua.nin.identity.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import ua.nin.identity.auth.exception.exceptions.EmailSenderException;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmtpEmailServiceImpl implements EmailSenderService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    @Value("${app.public-base-url}")
    private String publicBaseUrl;

    @Override
    @Async("appAsyncExecutor")
    public void sendEmailVerification(String email, String rawToken) {
        String link = UriComponentsBuilder.fromUriString(publicBaseUrl)
                .path("/api/v1/auth/email/verify")
                .queryParam("token", rawToken)
                .build()
                .toUriString();

        String subject = "Email verification";
        String html = """
                <div style="font-family: Arial, sans-serif;">
                  <h2>Email verification</h2>
                  <p>Click the link to verify your email:</p>
                  <p><a href="%s">%s</a></p>
                  <p>If you didn’t request this, ignore this email.</p>
                </div>
                """.formatted(link, link);

        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(from);
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException | MailException e) {
            log.error("Failed to send email verification: {}", e.getMessage(), e);
            throw new EmailSenderException("Failed to send verification email", e);
        }
    }

    @Override
    @Async("appAsyncExecutor")
    public void sendForgotPassword(String email, String rawToken){
        String link = UriComponentsBuilder.fromUriString(publicBaseUrl)
                .path("/api/v1/auth/password/reset")
                .queryParam("token", rawToken)
                .build()
                .toUriString();

        String subject = "Password reset";
        String html = """
                <div style="font-family: Arial, sans-serif;">
                  <h2>Password reset</h2>
                  <p>Click the link to reset your password:</p>
                  <p><a href="%s">%s</a></p>
                  <p>If you didn’t request this, ignore this email.</p>
                </div>
                """.formatted(link, link);

        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(from);
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException | MailException e) {
            log.error("Failed to send password reset email: {}", e.getMessage(), e);
            throw new EmailSenderException("Failed to send password reset email", e);
        }
    }
}