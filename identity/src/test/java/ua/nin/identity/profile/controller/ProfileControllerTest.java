package ua.nin.identity.profile.controller;

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
import ua.nin.identity.profile.dto.ProfileResponse;
import ua.nin.identity.profile.dto.PublicProfileResponse;
import ua.nin.identity.profile.dto.UpdateProfileRequest;
import ua.nin.identity.profile.model.Privacy;
import ua.nin.identity.profile.service.ProfileService;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProfileControllerTest {

    MockMvc mockMvc;

    ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    ProfileService profileService;

    @InjectMocks
    ProfileController profileController;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(profileController)
                .build();
    }

    @Test
    void me_returnsProfile() throws Exception {
        ProfileResponse response = new ProfileResponse(1, "user", "User", "bio", Privacy.PUBLIC, null, null, null, null);
        when(profileService.getMyProfile(1L)).thenReturn(response);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("1");

        mockMvc.perform(get("/api/v1/users/me")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user"))
                .andExpect(jsonPath("$.displayName").value("User"))
                .andExpect(jsonPath("$.bio").value("bio"));
    }

    @Test
    void update_returnsProfile() throws Exception {
        ProfileResponse response = new ProfileResponse(1, "user", "User", "bio", Privacy.PUBLIC, null, null, null, null);
        UpdateProfileRequest request = new UpdateProfileRequest("user", "User", "bio", Privacy.PUBLIC, null, null);
        when(profileService.updateMyProfile(1L, request)).thenReturn(response);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("1");

        mockMvc.perform(patch("/api/v1/users/me/profile")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user"))
                .andExpect(jsonPath("$.displayName").value("User"))
                .andExpect(jsonPath("$.bio").value("bio"));
    }

    @Test
    void publicByUsername_returnsProfile() throws Exception {
        PublicProfileResponse response = new PublicProfileResponse("user", "User", "bio");
        when(profileService.getPublicByUsername("user")).thenReturn(response);

        mockMvc.perform(get("/api/v1/users/by-username/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user"))
                .andExpect(jsonPath("$.displayName").value("User"))
                .andExpect(jsonPath("$.bio").value("bio"));
    }
}
