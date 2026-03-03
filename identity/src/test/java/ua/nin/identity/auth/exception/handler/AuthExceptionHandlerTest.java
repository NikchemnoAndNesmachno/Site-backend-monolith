package ua.nin.identity.auth.exception.handler;

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
import ua.nin.identity.auth.exception.exceptions.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthExceptionHandlerTest {

    @Mock
    WebRequest webRequest;
    @Mock
    ErrorAttributes errorAttributes;
    @InjectMocks
    AuthExceptionHandler handler;

    Map<String, Object> objectMap;

    @BeforeEach
    void init() {
        objectMap = new HashMap<>();
        objectMap.put("path", "/api/auth");
        objectMap.put("message", "test");
        objectMap.put("timestamp", new Date());
        objectMap.put("trace", "Internal Server Error");
        when(errorAttributes.getErrorAttributes(eq(webRequest),
                any(ErrorAttributeOptions.class))).thenReturn(objectMap);
    }

    static Stream<Arguments> unauthorizedExceptions() {
        return Stream.of(
                Arguments.of(new BadCredentialsException("bad")),
                Arguments.of(new InvalidTokenException("bad token")),
                Arguments.of(new InvalidRefreshTokenException("bad refresh")),
                Arguments.of(new MissingTokenException("missing")),
                Arguments.of(new InvalidJwtSecretException("invalid secret"))
        );
    }

    static Stream<Arguments> notFoundExceptions() {
        return Stream.of(
                Arguments.of(new NotFoundException("nf")),
                Arguments.of(new UserNotFoundException("unf")),
                Arguments.of(new CredentialNotFoundException("cnf"))
        );
    }

    static Stream<Arguments> conflictExceptions() {
        return Stream.of(
                Arguments.of(new ConflictException("conflict")),
                Arguments.of(new UsernameAlreadyExistsException("exists")),
                Arguments.of(new EmailAlreadyExistsException("exists"))
        );
    }

    static Stream<Arguments> forbiddenExceptions() {
        return Stream.of(
                Arguments.of(new ForbiddenException("forbidden")),
                Arguments.of(new RefreshReuseDetectedException("reuse")),
                Arguments.of(new TokenAlreadyUsedException("used")),
                Arguments.of(new TokenExpiredException("expired"))
        );
    }

    @ParameterizedTest
    @MethodSource("unauthorizedExceptions")
    void handleBadRequest(RuntimeException ex) {
        assertEquals(HttpStatus.UNAUTHORIZED, handler.handleUnauthorized(ex, webRequest).getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("notFoundExceptions")
    void handleNotFound(RuntimeException ex) {
        assertEquals(HttpStatus.NOT_FOUND, handler.handleNotFound(ex, webRequest).getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("conflictExceptions")
    void handleConflict(RuntimeException ex) {
        assertEquals(HttpStatus.CONFLICT, handler.handleConflict(ex, webRequest).getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("forbiddenExceptions")
    void handleForbidden(RuntimeException ex) {
        assertEquals(HttpStatus.FORBIDDEN, handler.handleForbidden(ex, webRequest).getStatusCode());
    }

    @Test
    void testHandleInternalServerErrorException() {
        RuntimeException exception = new RuntimeException("test");
        assertEquals(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ExceptionResponse("Internal server error")),
                handler.handleGeneric(exception, webRequest));
    }
}
