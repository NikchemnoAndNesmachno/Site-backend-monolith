package ua.nin.identity.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class SmtpEmailServiceImpl implements EmailSenderService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    @Value("${app.public-base-url}")
    private String publicBaseUrl;

    @Override
    public void sendEmailVerification(String to, String rawToken) {
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
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new IllegalStateException("Failed to send verification email", e);
        }
    }
}