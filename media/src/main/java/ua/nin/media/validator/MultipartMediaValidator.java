package ua.nin.media.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import ua.nin.media.config.MediaProperties;
import ua.nin.media.exception.exceptions.MediaValidationException;

import static ua.nin.common.util.StringHelperUtils.normalizeContentType;

@Component
@RequiredArgsConstructor
public class MultipartMediaValidator {

    private final MediaProperties properties;

    public void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new MediaValidationException(MediaValidationError.EMPTY_FILE, "Empty file");
        }

        long max = properties.getLimits().getMaxSizeBytes();
        if (max > 0 && file.getSize() > max) {
            throw new MediaValidationException(MediaValidationError.FILE_TOO_LARGE, "File too large");
        }

        String ct = normalizeContentType(file.getContentType());
        if (ct == null) {
            throw new MediaValidationException(MediaValidationError.MISSING_CONTENT_TYPE, "Missing Content-Type");
        }

        if (!(ct.startsWith("image/") || ct.startsWith("video/"))) {
            throw new MediaValidationException(MediaValidationError.UNSUPPORTED_MEDIA_TYPE, "Unsupported media type");
        }
    }
}