package ua.nin.identity.profile.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.nin.contract.profile.ProfileCreation;
import ua.nin.identity.profile.dto.*;
import ua.nin.identity.profile.exception.exceptions.ProfileNotFoundException;
import ua.nin.identity.profile.exception.exceptions.UsernameAlreadyTakenException;
import ua.nin.identity.profile.mapper.ProfileResponseMapper;
import ua.nin.identity.profile.mapper.PublicProfileResponseMapper;
import ua.nin.identity.profile.model.Privacy;
import ua.nin.identity.profile.model.Profile;
import ua.nin.identity.profile.repository.ProfileRepository;

import static ua.nin.common.constant.ErrorMessage.PROFILE_NOT_FOUND;
import static ua.nin.common.util.StringHelperUtils.normalizeAndTruncate;
import static ua.nin.common.util.StringHelperUtils.normalizeUsername;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService implements ProfileCreation {

    private final ProfileRepository profileRepository;
    private final ProfileResponseMapper profileResponseMapper;
    private final PublicProfileResponseMapper publicProfileResponseMapper;

    @Transactional(readOnly = true)
    public ProfileResponse getMyProfile(long userId) {
        Profile p = profileRepository.findById(userId)
                .orElseThrow(() -> new ProfileNotFoundException(PROFILE_NOT_FOUND));

        return profileResponseMapper.toDto(p);
    }

    @Transactional
    public ProfileResponse updateMyProfile(long userId, UpdateProfileRequest req) {
        Profile p = profileRepository.findById(userId)
                .orElseThrow(() -> new ProfileNotFoundException(PROFILE_NOT_FOUND));

        String normalized = normalizeUsername(req.username());
        if (normalized != null) {
            if (profileRepository.existsByUsernameAndUserIdNot(normalized, userId)) {
                throw new UsernameAlreadyTakenException("Username already taken");
            }
            p.setUsername(normalized);
        }

        if (req.displayName() != null) p.setDisplayName(normalizeAndTruncate(req.displayName(), 64));
        if (req.bio() != null) p.setBio(normalizeAndTruncate(req.bio(), 500));
        if (req.privacy() != null) p.setPrivacy(req.privacy());
        if (req.locale() != null) p.setLocale(normalizeAndTruncate(req.locale(), 16));
        if (req.timezone() != null) p.setTimezone(normalizeAndTruncate(req.timezone(), 64));

        return profileResponseMapper.toDto(p);
    }

    @Transactional(readOnly = true)
    public PublicProfileResponse getPublicByUsername(String username) {
        String normalized = normalizeUsername(username);
        Profile p = profileRepository.findByUsername(normalized)
                .orElseThrow(() -> new ProfileNotFoundException(PROFILE_NOT_FOUND));

        if (p.getPrivacy() != Privacy.PUBLIC) {
            // TODO MVP: FRIENDS_ONLY та PRIVATE не показуєм взагалі
            throw new ProfileNotFoundException(PROFILE_NOT_FOUND);
        }

        return publicProfileResponseMapper.toDto(p);
    }

    @Override
    public void createProfile(Long userId, String username) {
        Profile profile = Profile.builder()
                .userId(userId)
                .username(username)
                .displayName(username)
                .privacy(Privacy.PUBLIC)
                .build();
        profileRepository.save(profile);
    }
}
