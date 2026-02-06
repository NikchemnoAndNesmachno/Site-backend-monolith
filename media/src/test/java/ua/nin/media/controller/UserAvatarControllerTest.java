package ua.nin.media.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ua.nin.media.dto.AvatarUploadResponse;
import ua.nin.media.service.UserAvatarService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserAvatarControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserAvatarService userAvatarService;

    @InjectMocks
    private UserAvatarController userAvatarController;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(userAvatarController).build();
    }

    @Test
    void upload_returnsResponse() throws Exception {
        AvatarUploadResponse response = AvatarUploadResponse.builder().avatarId(1L).avatarAssetId(2L).build();
        when(userAvatarService.uploadAvatar(any(Long.class), any())).thenReturn(response);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("1");

        MockMultipartFile avatar = new MockMultipartFile("avatar", "avatar.png", "image/png", "data".getBytes());

        mockMvc.perform(multipart("/api/v1/avatar/upload")
                        .file(avatar)
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.avatarId").value(1));
    }

    @Test
    void delete_returnsNoContent() throws Exception {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("1");

        mockMvc.perform(delete("/api/v1/avatar/1")
                        .principal(authentication))
                .andExpect(status().isNoContent());
    }
}
