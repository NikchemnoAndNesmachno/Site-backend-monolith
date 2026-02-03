package ua.nin.media.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;
import ua.nin.media.dto.AvatarUploadResponse;
import ua.nin.media.model.MediaAsset;
import ua.nin.media.model.UserAvatar;
import ua.nin.media.repository.UserAvatarRepository;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAvatarServiceTest {

    @Mock
    private UserAvatarRepository userAvatarRepository;
    @Mock
    private MediaService mediaService;

    @InjectMocks
    private UserAvatarService userAvatarService;

    @Test
    void uploadAvatar_savesAvatar() {
        MediaAsset asset = MediaAsset.builder().id(10L).build();
        when(mediaService.storeSingle(any(), any())).thenReturn(asset);

        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", "data".getBytes());

        AvatarUploadResponse response = userAvatarService.uploadAvatar(2L, file);

        assertEquals(10L, response.avatarAssetId());
        verify(userAvatarRepository).save(any(UserAvatar.class));
    }

    @Test
    void deleteAvatar_notOwner_throws() {
        assertThatThrownBy(() -> userAvatarService.deleteAvatar(1L, 2L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("not allowed");

        verifyNoInteractions(userAvatarRepository);
    }
}
