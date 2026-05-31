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
import ua.nin.identity.auth.dto.*;
import ua.nin.identity.auth.service.AuthService;
import ua.nin.identity.auth.service.HttpCookieService;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    MockMvc mockMvc;

    ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AuthService authService;

    @Spy
    private HttpCookieService httpCookieService = new HttpCookieService(14L, false);

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
        AuthResponse response = new AuthResponse("access", "Bearer", 600L, 1L, "user@gmail.com", "USER");
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

    @Test
    void refresh_rotatesRefreshCookie() throws Exception {
        AuthResponse response = new AuthResponse("new-access", "Bearer", 600L, 1L, "user@gmail.com", "USER");
        when(authService.refresh(any(), any(), any())).thenReturn(new AuthResult(response, "new-refresh-token"));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(new jakarta.servlet.http.Cookie("refresh_token", "old-refresh-token")))
                .andExpect(status().isOk())
                .andExpect(header().string("Set-Cookie", containsString("refresh_token=")))
                .andExpect(cookie().value("refresh_token", "new-refresh-token"))
                .andExpect(jsonPath("$.accessToken").value("new-access"));
    }

    @Test
    void logout_clearsRefreshCookie() throws Exception {
        doNothing().when(authService).logout(any());

        mockMvc.perform(post("/api/v1/auth/logout")
                        .cookie(new jakarta.servlet.http.Cookie("refresh_token", "refresh-token")))
                .andExpect(status().isNoContent())
                .andExpect(header().string("Set-Cookie", containsString("Max-Age=0")));

        verify(authService).logout("refresh-token");
    }

    @Test
    void logoutAll_returnsNoContent() throws Exception {
        doNothing().when(authService).logoutAll(1L);

        org.springframework.security.core.Authentication authentication = org.mockito.Mockito.mock(org.springframework.security.core.Authentication.class);
        when(authentication.getName()).thenReturn("1");

        mockMvc.perform(post("/api/v1/auth/logout-all").principal(authentication))
                .andExpect(status().isNoContent());
    }

    @Test
    void me_returnsCurrentUser() throws Exception {
        when(authService.me(1L)).thenReturn(new MeResponse(1L, "user@site.com", "ACTIVE", "USER"));

        org.springframework.security.core.Authentication authentication = org.mockito.Mockito.mock(org.springframework.security.core.Authentication.class);
        when(authentication.getName()).thenReturn("1");

        mockMvc.perform(get("/api/v1/auth/me").principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value("user@site.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.role").value("USER"));
    }
}
