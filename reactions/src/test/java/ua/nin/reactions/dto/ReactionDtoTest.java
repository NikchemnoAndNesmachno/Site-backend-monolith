package ua.nin.reactions.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ReactionDtoTest {

    @ParameterizedTest
    @MethodSource("provideTargetTypeValidValues")
    void validTargetTypeInPutReactionRequestDtoTest(String targetType){
        var dto = PutReactionRequest.builder()
                .targetType(targetType)
                .targetId(1L)
                .reactionCode("LIKE")
                .build();

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        final Validator validator = factory.getValidator();

        Set<ConstraintViolation<PutReactionRequest>> constraintViolations =
                validator.validate(dto);

        assertThat(constraintViolations).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("provideTargetTypeInvalidValues")
    void invalidNameInUserManagementUpdateDtoTest(String targetType) {
        var dto = PutReactionRequest.builder()
                .targetType(targetType)
                .targetId(1L)
                .reactionCode("LIKE")
                .build();

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        final Validator validator = factory.getValidator();

        Set<ConstraintViolation<PutReactionRequest>> constraintViolations =
                validator.validate(dto);

        assertThat(constraintViolations).hasSize(1);
    }

    private static Stream<Arguments> provideTargetTypeValidValues() {
        return Stream.of(
                Arguments.of("VIDEO"),
                Arguments.of("IMAGE"),
                Arguments.of("PREVIEW"),
                Arguments.of("VIDEO_"));
    }

    private static Stream<Arguments> provideTargetTypeInvalidValues() {
        return Stream.of(
                Arguments.of("video"),
                Arguments.of("Preview"),
                Arguments.of("VIDEO/MP4"),
                Arguments.of("video/mp4"),
                Arguments.of("IMAGe"));
    }
}
