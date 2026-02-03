package ua.nin.views.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ua.nin.views.dto.ViewCountsResponse;
import ua.nin.views.service.ViewService;

import java.time.Instant;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ViewControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ViewService viewService;

    @InjectMocks
    private ViewController viewController;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(viewController).build();
    }

    @Test
    void recordView_guestUsesXff() throws Exception {
        mockMvc.perform(post("/api/v1/views")
                        .param("targetType", "VIDEO")
                        .param("targetId", "1")
                        .header("X-Forwarded-For", "1.2.3.4, 5.6.7.8"))
                .andExpect(status().isNoContent());

        verify(viewService).recordView(eq("VIDEO"), eq(1L), isNull(), any(), eq("1.2.3.4"));
    }

    @Test
    void recordView_authenticatedUsesUserId() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("7");

        mockMvc.perform(post("/api/v1/views")
                        .param("targetType", "VIDEO")
                        .param("targetId", "1")
                        .principal(auth))
                .andExpect(status().isNoContent());

        verify(viewService).recordView(eq("VIDEO"), eq(1L), eq(7L), any(), any());
    }

    @Test
    void viewCounts_returnsResponse() throws Exception {
        ViewCountsResponse response = new ViewCountsResponse("VIDEO", 1L, 10L, 5L, Instant.now());
        when(viewService.getCounts("VIDEO", 1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/views")
                        .param("targetType", "VIDEO")
                        .param("targetId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalViews").value(10));
    }
}
