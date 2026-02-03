package ua.nin.identity.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ua.nin.identity.auth.dto.AuthResponse;
import ua.nin.identity.auth.dto.AuthResult;
import ua.nin.identity.auth.dto.LoginRequest;
import ua.nin.identity.auth.dto.RegisterRequest;
import ua.nin.identity.auth.service.AuthService;
import ua.nin.identity.auth.service.HttpCookieService;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    MockMvc mockMvc;

    ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AuthService authService;

    @Spy
    private HttpCookieService httpCookieService = new HttpCookieService();

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .build();
    }

    @Test
    void register_returnsCreated() throws Exception {
        doNothing().when(authService).register(any(RegisterRequest.class));

        RegisterRequest request = RegisterRequest.builder()
                .email("user@site.com")
                .username("user")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void login_setsRefreshCookie() throws Exception {
        LoginRequest request = new LoginRequest("user@site.com", "password123");
        AuthResponse response = new AuthResponse("access", "Bearer", 600L, 1L, "USER");
        when(authService.login(any(LoginRequest.class), any(), any()))
                .thenReturn(new AuthResult(response, "refresh-token"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().string("Set-Cookie", containsString("refresh_token=")))
                .andExpect(cookie().exists("refresh_token"))
                .andExpect(cookie().value("refresh_token", "refresh-token"))
                .andExpect(jsonPath("$.accessToken").value("access"));
    }
}
