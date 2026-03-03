package ua.nin.media.exception.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;
import ua.nin.common.exception.response.ExceptionResponse;
import ua.nin.media.exception.exceptions.*;
import ua.nin.media.validator.MediaValidationError;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MediaExceptionHandlerTest {

    @Mock
    WebRequest webRequest;
    @Mock
    ErrorAttributes errorAttributes;
    @InjectMocks
    MediaExceptionHandler handler;

    Map<String, Object> objectMap;

    @BeforeEach
    void init() {
        objectMap = new HashMap<>();
        objectMap.put("path", "/api/media");
        objectMap.put("message", "test");
        objectMap.put("timestamp", new Date());
        objectMap.put("trace", "Internal Server Error");
        when(errorAttributes.getErrorAttributes(eq(webRequest),
                any(ErrorAttributeOptions.class))).thenReturn(objectMap);
    }

    static Stream<Arguments> notFoundExceptions() {
        return Stream.of(
                Arguments.of(new MediaNotFoundException("not found")),
                Arguments.of(new FileNotFoundException("not found")),
                Arguments.of(new AvatarNotFoundException("not found")),
                Arguments.of(new VideoNotFound("not found"))
        );
    }

    static Stream<Arguments> storageExceptions() {
        return Stream.of(
                Arguments.of(new FileMovingException("move")),
                Arguments.of(new FileStoringException("store"))
        );
    }

    static Stream<Arguments> badRequestExceptions() {
        return Stream.of(
                Arguments.of(new MediaValidationException(MediaValidationError.MISSING_CONTENT_TYPE, "invalid"))
        );
    }

    static Stream<Arguments> forbiddenExceptions() {
        return Stream.of(
                Arguments.of(new AvatarForbiddenDeletionException("forbidden")),
                Arguments.of(new VideoForbiddenDeletionException("forbidden"))
        );
    }

    @ParameterizedTest
    @MethodSource("notFoundExceptions")
    void handleNotFound(RuntimeException ex) {
        assertEquals(HttpStatus.NOT_FOUND, handler.handleNotFound(ex, webRequest).getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("badRequestExceptions")
    void handleBadRequest(RuntimeException ex) {
        assertEquals(HttpStatus.BAD_REQUEST, handler.handleBadRequest(ex, webRequest).getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("forbiddenExceptions")
    void handleForbidden(RuntimeException ex) {
        assertEquals(HttpStatus.FORBIDDEN, handler.handleForbidden(ex, webRequest).getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("storageExceptions")
    void handleStorageErrors(RuntimeException ex) {
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, handler.handleStorageErrors(ex, webRequest).getStatusCode());
    }


    @Test
    void testHandleInternalServerErrorException() {
        RuntimeException exception = new RuntimeException("test");
        assertEquals(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ExceptionResponse("Internal server error")),
                handler.handleGeneric(exception, webRequest));
    }
}