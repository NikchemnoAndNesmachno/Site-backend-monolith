package ua.nin.identity.profile.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.nin.identity.profile.dto.ProfileResponse;
import ua.nin.identity.profile.dto.PublicProfileResponse;
import ua.nin.identity.profile.dto.UpdateProfileRequest;
import ua.nin.identity.profile.exception.exceptions.ProfileNotFoundException;
import ua.nin.identity.profile.exception.exceptions.UsernameAlreadyTakenException;
import ua.nin.identity.profile.mapper.ProfileResponseMapper;
import ua.nin.identity.profile.mapper.PublicProfileResponseMapper;
import ua.nin.identity.profile.model.Privacy;
import ua.nin.identity.profile.model.Profile;
import ua.nin.identity.profile.repository.ProfileRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private ProfileResponseMapper profileResponseMapper;
    @Mock
    private PublicProfileResponseMapper publicProfileResponseMapper;

    @InjectMocks
    private ProfileService profileService;

    @Test
    void getMyProfile_missing_throws() {
        when(profileRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.getMyProfile(1L))
                .isInstanceOf(ProfileNotFoundException.class)
                .hasMessageContaining("Profile not found");
    }

    @Test
    void updateMyProfile_usernameTaken_throws() {
        Profile profile = Profile.builder().userId(1L).username("old").build();
        when(profileRepository.findById(1L)).thenReturn(Optional.of(profile));
        when(profileRepository.existsByUsernameAndUserIdNot("new-name", 1L)).thenReturn(true);

        UpdateProfileRequest request = new UpdateProfileRequest("new-name", null, null, null, null, null);

        assertThatThrownBy(() -> profileService.updateMyProfile(1L, request))
                .isInstanceOf(UsernameAlreadyTakenException.class)
                .hasMessageContaining("Username already taken");
    }

    @Test
    void updateMyProfile_success_updatesFields() {
        long userId = 1L;
        Profile profile = Profile.builder().userId(userId).username("old").build();
        when(profileRepository.findById(1L)).thenReturn(Optional.of(profile));

        UpdateProfileRequest request = new UpdateProfileRequest("NewName", "Display", "bio", Privacy.PUBLIC, "en", "UTC");
        ProfileResponse response = new ProfileResponse(userId, "NewName", "Display", "bio", Privacy.PUBLIC, "en", "UTC", null, null);
        when(profileResponseMapper.toDto(profile)).thenReturn(response);

        ProfileResponse result = profileService.updateMyProfile(1L, request);

        assertEquals("NewName", result.username());
        assertEquals("Display", result.displayName());
    }

    @Test
    void getPublicByUsername_privateProfile_throws() {
        Profile profile = Profile.builder().userId(1L).username("user").privacy(Privacy.PRIVATE).build();
        when(profileRepository.findByUsername("user")).thenReturn(Optional.of(profile));

        assertThatThrownBy(() -> profileService.getPublicByUsername("user"))
                .isInstanceOf(ProfileNotFoundException.class)
                .hasMessageContaining("Profile not found");
    }

    @Test
    void getPublicByUsername_success() {
        Profile profile = Profile.builder().userId(1L).username("user").privacy(Privacy.PUBLIC).build();
        PublicProfileResponse response = new PublicProfileResponse("user", "User", null);
        when(profileRepository.findByUsername("user")).thenReturn(Optional.of(profile));
        when(publicProfileResponseMapper.toDto(profile)).thenReturn(response);

        PublicProfileResponse result = profileService.getPublicByUsername("user");

        assertEquals("user", result.username());
    }
}
