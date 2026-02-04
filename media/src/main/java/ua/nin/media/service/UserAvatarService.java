package ua.nin.media.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ua.nin.media.dto.AvatarUploadResponse;
import ua.nin.media.exception.exceptions.AvatarForbiddenDeletionException;
import ua.nin.media.exception.exceptions.AvatarNotFoundException;
import ua.nin.media.model.MediaAsset;
import ua.nin.media.model.MediaKind;
import ua.nin.media.model.UserAvatar;
import ua.nin.media.repository.UserAvatarRepository;

import static ua.nin.common.constant.ErrorMessage.*;

@Service
@RequiredArgsConstructor
public class UserAvatarService {

    private final UserAvatarRepository userAvatarRepository;
    private final MediaService mediaService;

    @Transactional
    public AvatarUploadResponse uploadAvatar(long ownerUserId, MultipartFile avatarFile) {
        MediaAsset asset = mediaService.storeSingle(avatarFile, MediaKind.IMAGE);

        UserAvatar avatar = UserAvatar.builder()
                .ownerUserId(ownerUserId)
                .mediaAsset(asset)
                .build();

        userAvatarRepository.save(avatar);

        return AvatarUploadResponse.builder()
                .avatarId(avatar.getOwnerUserId())
                .avatarAssetId(asset.getId())
                .build();
    }

    @Transactional
    public void deleteAvatar(long avatarId, long userId) {
        if (avatarId != userId) {
            throw new AvatarForbiddenDeletionException(USER_NOT_ALLOWED_TO_DELETE_AVATAR);
        }

        UserAvatar avatar = userAvatarRepository.findById(avatarId)
                .orElseThrow(() -> new AvatarNotFoundException(AVATAR_NOT_FOUND));

        userAvatarRepository.delete(avatar);
    }
}
