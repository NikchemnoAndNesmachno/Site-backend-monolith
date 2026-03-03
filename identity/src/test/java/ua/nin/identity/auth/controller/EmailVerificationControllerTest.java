package ua.nin.identity.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ua.nin.identity.auth.dto.ResendVerifyRequest;
import ua.nin.identity.auth.service.EmailVerificationService;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class EmailVerificationControllerTest {

    MockMvc mockMvc;

    ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    EmailVerificationService emailVerificationService;

    @InjectMocks
    EmailVerificationController emailVerificationController;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(emailVerificationController)
                .build();
    }

    @Test
    void verify_returnsOk() throws Exception {
        doNothing().when(emailVerificationService).verify("verify-token");

        mockMvc.perform(get("/api/v1/auth/email/verify")
                        .param("token", "verify-token"))
                .andExpect(status().isOk());

        verify(emailVerificationService).verify("verify-token");
    }

    @Test
    void resend_returnsNoContent() throws Exception {
        ResendVerifyRequest request = new ResendVerifyRequest("user@site.com");
        doNothing().when(emailVerificationService).resend("user@site.com");

        mockMvc.perform(post("/api/v1/auth/email/resend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(emailVerificationService).resend("user@site.com");
    }
}
