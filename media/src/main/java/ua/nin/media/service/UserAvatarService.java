package ua.nin.media.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import ua.nin.media.dto.AvatarUploadResponse;
import ua.nin.media.model.MediaAsset;
import ua.nin.media.model.MediaKind;
import ua.nin.media.model.UserAvatar;
import ua.nin.media.repository.UserAvatarRepository;

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
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to delete this avatar image");
        }

        UserAvatar avatar = userAvatarRepository.findById(avatarId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User avatar not found"));

        userAvatarRepository.delete(avatar);
    }
}
