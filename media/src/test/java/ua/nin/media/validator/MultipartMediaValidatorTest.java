package ua.nin.media.validator;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import ua.nin.media.config.MediaProperties;
import ua.nin.media.exception.exceptions.MediaValidationException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MultipartMediaValidatorTest {

    @Test
    void validate_rejectsEmptyFile() {
        MediaProperties properties = new MediaProperties();
        MultipartMediaValidator validator = new MultipartMediaValidator(properties);

        MockMultipartFile file = new MockMultipartFile("file", new byte[0]);

        assertThatThrownBy(() -> validator.validate(file))
                .isInstanceOf(MediaValidationException.class)
                .hasMessageContaining("Empty file");
    }

    @Test
    void validate_rejectsUnsupportedType() {
        MediaProperties properties = new MediaProperties();
        MultipartMediaValidator validator = new MultipartMediaValidator(properties);

        MockMultipartFile file = new MockMultipartFile("file", "file.txt", "text/plain", "data".getBytes());

        assertThatThrownBy(() -> validator.validate(file))
                .isInstanceOf(MediaValidationException.class)
                .hasMessageContaining("Unsupported media type");
    }

    @Test
    void validate_rejectsTooLarge() {
        MediaProperties properties = new MediaProperties();
        properties.getLimits().setMaxSizeBytes(1);
        MultipartMediaValidator validator = new MultipartMediaValidator(properties);

        MockMultipartFile file = new MockMultipartFile("file", "file.png", "image/png", "data".getBytes());

        assertThatThrownBy(() -> validator.validate(file))
                .isInstanceOf(MediaValidationException.class)
                .hasMessageContaining("File too large");
    }

    @Test
    void validate_acceptsImage() {
        MediaProperties properties = new MediaProperties();
        MultipartMediaValidator validator = new MultipartMediaValidator(properties);

        MockMultipartFile file = new MockMultipartFile("file", "file.png", "image/png", "data".getBytes());

        validator.validate(file);
    }
}
