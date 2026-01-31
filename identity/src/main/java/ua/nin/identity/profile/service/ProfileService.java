package ua.nin.identity.profile.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.nin.identity.profile.dto.*;
import ua.nin.identity.profile.exception.exceptions.ProfileNotFoundException;
import ua.nin.identity.profile.exception.exceptions.UsernameAlreadyTakenException;
import ua.nin.identity.profile.mapper.ProfileResponseMapper;
import ua.nin.identity.profile.mapper.PublicProfileResponseMapper;
import ua.nin.identity.profile.model.Privacy;
import ua.nin.identity.profile.model.Profile;
import ua.nin.identity.profile.repository.ProfileRepository;

import static ua.nin.common.util.StringHelperUtils.normalizeAndTruncate;
import static ua.nin.common.util.StringHelperUtils.normalizeUsername;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final ProfileResponseMapper profileResponseMapper;
    private final PublicProfileResponseMapper publicProfileResponseMapper;

    @Transactional(readOnly = true)
    public ProfileResponse getMyProfile(long userId) {
        Profile p = profileRepository.findById(userId)
                .orElseThrow(() -> new ProfileNotFoundException("Profile not found"));

        return profileResponseMapper.toDto(p);
    }

    @Transactional
    public ProfileResponse updateMyProfile(long userId, UpdateProfileRequest req) {
        Profile p = profileRepository.findById(userId)
                .orElseThrow(() -> new ProfileNotFoundException("Profile not found"));

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

    @Transactional
    public void setAvatar(long userId, long mediaId) {
        Profile p = profileRepository.findById(userId)
                .orElseThrow(() -> new ProfileNotFoundException("Profile not found"));
        p.setAvatarMediaId(mediaId);
    }

    @Transactional(readOnly = true)
    public PublicProfileResponse getPublicByUsername(String username) {
        String normalized = normalizeUsername(username);
        Profile p = profileRepository.findByUsername(normalized)
                .orElseThrow(() -> new ProfileNotFoundException("Profile not found"));

        if (p.getPrivacy() != Privacy.PUBLIC) {
            // TODO MVP: FRIENDS_ONLY та PRIVATE не показуєм взагалі
            throw new ProfileNotFoundException("Profile not found");
        }

        return publicProfileResponseMapper.toDto(p);
    }

}
