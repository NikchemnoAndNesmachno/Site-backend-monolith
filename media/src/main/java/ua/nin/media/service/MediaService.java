package ua.nin.media.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import ua.nin.media.dto.MediaMetaResponse;
import ua.nin.media.dto.MediaUploadResponse;
import ua.nin.media.mapper.MediaMetaResponseMapper;
import ua.nin.media.model.*;
import ua.nin.media.repository.MediaAssetRepository;
import ua.nin.media.repository.MediaBundleItemRepository;
import ua.nin.media.repository.MediaBundleRepository;
import ua.nin.media.storage.MediaStorage;
import ua.nin.media.validator.MultipartMediaValidator;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;

import static ua.nin.common.util.StringHelperUtils.normalizeContentType;
import static ua.nin.media.util.MediaUtils.*;

@Service
@RequiredArgsConstructor
public class MediaService {

    private final MediaAssetRepository assetRepository;
    private final MediaBundleRepository bundleRepository;
    private final MediaBundleItemRepository bundleItemRepository;
    private final MediaStorage storage;
    private final MediaMetaResponseMapper mediaMetaResponseMapper;
    private final MultipartMediaValidator multipartMediaValidator;

    @Transactional
    public MediaUploadResponse uploadAsset(long ownerUserId, MultipartFile file) {
        MediaAsset asset = storeSingle(file, null);
        return new MediaUploadResponse(asset.getId());
    }

    @Transactional
    public MediaBundle createBundle(long ownerUserId, BundleType bundleType) {
        MediaBundle bundle = MediaBundle.builder()
                .ownerUserId(ownerUserId)
                .type(bundleType)
                .build();
        return bundleRepository.save(bundle);
    }

    @Transactional
    public MediaBundleItem createBundleItem(long bundleId, BundleItemRole bundleItemRole, long mediaId) {
        return bundleItemRepository.save(MediaBundleItem.builder()
                .bundleId(bundleId)
                .role(bundleItemRole)
                .mediaId(mediaId)
                .build());
    }

    @Transactional(readOnly = true)
    public MediaMetaResponse metaInformation(long id) {
        MediaAsset asset = assetRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Media not found"));
        return mediaMetaResponseMapper.toDto(asset);
    }

    @Transactional
    public void deleteAsset(long id) {
        MediaAsset asset = assetRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Media not found"));

        asset.setDeletedAt(Instant.now());
        assetRepository.save(asset);

        try {
            storage.delete(asset.getStorageKey());
        } catch (Exception ignored) {
            // best-effort delete; DB state is the source of truth
        }
    }

    @Transactional(readOnly = true)
    public InputStream open(long id) {
        MediaAsset asset = assetRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Media not found"));
        try {
            return storage.open(asset.getStorageKey());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found on storage");
        }
    }

    @Transactional(readOnly = true)
    public MediaAsset getAssetOrThrow(long id) {
        return assetRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Media not found"));
    }

    @Transactional
    public MediaAsset storeSingle(MultipartFile file, MediaKind forcedKind) {
        multipartMediaValidator.validate(file);

        String contentType = normalizeContentType(file.getContentType());
        MediaKind kind = forcedKind != null ? forcedKind : detectKind(contentType);

        String originalFilename = sanitizeOriginalFilename(file.getOriginalFilename());
        String extension = pickExtension(originalFilename, contentType);

        String tmpStorageKey = buildTmpStorageKey(extension);

        String sha256;
        long sizeBytes = file.getSize();

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            try (InputStream raw = file.getInputStream();
                 InputStream in = new DigestInputStream(raw, md)) {
                storage.save(tmpStorageKey, in);
            }
            sha256 = HexFormat.of().formatHex(md.digest());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Cannot store media");
        }

        var existing = assetRepository.findBySha256AndSizeBytesAndDeletedAtIsNull(sha256, sizeBytes);
        if (existing.isPresent()) {
            try {
                storage.delete(tmpStorageKey);
            }  catch (Exception ignored) {}
            return existing.get();
        }

        String storageKey = buildStorageKey(extension, sha256);

        try {
            storage.move(tmpStorageKey, storageKey);
        } catch (IOException ignored) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Cannot finalize media");
        }

        MediaAsset asset = MediaAsset.builder()
                .kind(kind)
                .contentType(contentType)
                .originalFilename(originalFilename)
                .storageKey(storageKey)
                .sizeBytes(file.getSize())
                .sha256(sha256)
                .build();

        return assetRepository.save(asset);
    }
}

