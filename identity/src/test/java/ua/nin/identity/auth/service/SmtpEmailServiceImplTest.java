package ua.nin.identity.auth.service;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.test.util.ReflectionTestUtils;
import ua.nin.identity.auth.exception.exceptions.EmailSenderException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmtpEmailServiceImplTest {

    @Mock
    private org.springframework.mail.javamail.JavaMailSender mailSender;

    @InjectMocks
    private SmtpEmailServiceImpl service;

    @Captor
    private ArgumentCaptor<MimeMessage> messageCaptor;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "from", "test@nin.ua");
        ReflectionTestUtils.setField(service, "publicBaseUrl", "https://test.nin.ua");
    }

    @Test
    void sendEmailVerification_buildsAndSendsHtmlEmail_withProperLinkAndHeaders() throws Exception {
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
        String to = "user@example.com";
        String token = "raw-token-123";

        service.sendEmailVerification(to, token);

        verify(mailSender).send(messageCaptor.capture());
        MimeMessage msg = messageCaptor.getValue();

        assertNotNull(msg);

        // headers
        assertEquals("Email verification", msg.getSubject());
        assertEquals("test@nin.ua", msg.getFrom()[0].toString());
        assertEquals(to, msg.getAllRecipients()[0].toString());

        // content
        Object content = msg.getContent();
        assertNotNull(content);

        // MimeMessageHelper робить multipart/related, тому тут часто не String
        String body = MimeMessageTestUtil.extractText(msg);
        assertTrue(body.contains("Email verification"));

        String expectedLink = "https://test.nin.ua/api/v1/auth/email/verify?token=" + token;
        assertTrue(body.contains(expectedLink), "Body must contain verification link with token");
        assertTrue(body.contains("<a href="), "Body must contain HTML anchor tag");
    }

    @Test
    void sendPasswordReset_buildsAndSendsHtmlEmail_withProperLinkAndHeaders() throws Exception {
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
        String to = "user@example.com";
        String token = "raw-token-123";

        service.sendForgotPassword(to, token);

        verify(mailSender).send(messageCaptor.capture());
        MimeMessage msg = messageCaptor.getValue();

        assertNotNull(msg);

        // headers
        assertEquals("Password reset", msg.getSubject());
        assertEquals("test@nin.ua", msg.getFrom()[0].toString());
        assertEquals(to, msg.getAllRecipients()[0].toString());

        // content
        Object content = msg.getContent();
        assertNotNull(content);

        // MimeMessageHelper робить multipart/related, тому тут часто не String
        String body = MimeMessageTestUtil.extractText(msg);
        assertTrue(body.contains("Password reset"));

        String expectedLink = "https://test.nin.ua/api/v1/auth/password/reset?token=" + token;
        assertTrue(body.contains(expectedLink), "Body must contain verification link with token");
        assertTrue(body.contains("<a href="), "Body must contain HTML anchor tag");
    }

    @Test
    void sendEmailVerification_whenMailSenderFails_thenThrowsEmailSenderException() {
        MimeMessage msg = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(msg);

        doThrow(new MailSendException("boom"))
                .when(mailSender).send(msg);

        EmailSenderException ex = assertThrows(
                EmailSenderException.class,
                () -> service.sendEmailVerification("test@nin.ua", "t")
        );

        assertTrue(ex.getMessage().contains("Failed to send verification email"));
        verify(mailSender).send(msg);
    }

    @Test
    void sendPasswordReset_whenMailSenderFails_thenThrowsEmailSenderException() {
        MimeMessage msg = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(msg);

        doThrow(new MailSendException("boom"))
                .when(mailSender).send(msg);

        EmailSenderException ex = assertThrows(
                EmailSenderException.class,
                () -> service.sendForgotPassword("test@nin.ua", "t")
        );

        assertTrue(ex.getMessage().contains("Failed to send password reset email"));
        verify(mailSender).send(msg);
    }

    /**
     * Утіліта, щоб дістати текст/HTML із MimeMessage, навіть якщо там multipart.
     */
    static final class MimeMessageTestUtil {
        static String extractText(MimeMessage message) throws Exception {
            Object content = message.getContent();
            if (content instanceof String s) {
                return s;
            }
            if (content instanceof jakarta.mail.Multipart mp) {
                return extractFromMultipart(mp);
            }
            return String.valueOf(content);
        }

        private static String extractFromMultipart(jakarta.mail.Multipart mp) throws Exception {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mp.getCount(); i++) {
                var part = mp.getBodyPart(i);
                Object partContent = part.getContent();
                if (partContent instanceof String s) {
                    sb.append(s);
                } else if (partContent instanceof jakarta.mail.Multipart nested) {
                    sb.append(extractFromMultipart(nested));
                }
            }
            return sb.toString();
        }
    }
}