package ua.nin.identity.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ua.nin.identity.auth.dto.ChangePasswordRequest;
import ua.nin.identity.auth.dto.ForgotPasswordRequest;
import ua.nin.identity.auth.dto.ResetPasswordRequest;
import ua.nin.identity.auth.service.PasswordService;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PasswordControllerTest {

    MockMvc mockMvc;

    ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    PasswordService passwordService;

    @InjectMocks
    PasswordController passwordController;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(passwordController)
                .build();
    }

    @Test
    void forgot_returnsNoContent() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest("user@site.com");
        doNothing().when(passwordService).forgot("user@site.com");

        mockMvc.perform(post("/api/v1/auth/password/forgot")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(passwordService).forgot("user@site.com");
    }

    @Test
    void reset_returnsNoContent() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest("newPassword123");
        doNothing().when(passwordService).reset("reset-token", "newPassword123");

        mockMvc.perform(post("/api/v1/auth/password/reset")
                        .param("token", "reset-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(passwordService).reset("reset-token", "newPassword123");
    }

    @Test
    void change_returnsNoContent() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("oldPass123", "newPassword123");
        doNothing().when(passwordService).change(1L, "oldPass123", "newPassword123");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("1");

        mockMvc.perform(post("/api/v1/auth/password/change")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(passwordService).change(1L, "oldPass123", "newPassword123");
    }
}
