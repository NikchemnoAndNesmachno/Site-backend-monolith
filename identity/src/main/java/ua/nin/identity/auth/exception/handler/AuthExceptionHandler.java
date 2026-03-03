package ua.nin.identity.auth.exception.handler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import ua.nin.common.exception.response.ExceptionResponse;
import ua.nin.identity.auth.controller.AuthController;
import ua.nin.identity.auth.controller.EmailVerificationController;
import ua.nin.identity.auth.controller.PasswordController;
import ua.nin.identity.auth.exception.exceptions.*;

import static ua.nin.common.exception.util.ExceptionHandlerUtils.buildContext;

@Slf4j
@AllArgsConstructor
@RestControllerAdvice(assignableTypes = {AuthController.class, PasswordController.class, EmailVerificationController.class})
public class AuthExceptionHandler {

    private final ErrorAttributes errorAttributes;

    @ExceptionHandler({BadCredentialsException.class, InvalidTokenException.class, InvalidRefreshTokenException.class, MissingTokenException.class})
    public ResponseEntity<ExceptionResponse> handleUnauthorized(RuntimeException ex, WebRequest request) {
        log.warn("Auth unauthorized {}", buildContext(request, errorAttributes, ex));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ExceptionResponse(ex.getMessage()));
    }

    @ExceptionHandler({ForbiddenException.class, RefreshReuseDetectedException.class, TokenAlreadyUsedException.class, TokenExpiredException.class})
    public ResponseEntity<ExceptionResponse> handleForbidden(RuntimeException ex, WebRequest request) {
        log.warn("Auth forbidden {}", buildContext(request, errorAttributes, ex));
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ExceptionResponse(ex.getMessage()));
    }

    @ExceptionHandler({NotFoundException.class, UserNotFoundException.class, CredentialNotFoundException.class})
    public ResponseEntity<ExceptionResponse> handleNotFound(RuntimeException ex, WebRequest request) {
        log.warn("Auth not found {}", buildContext(request, errorAttributes, ex));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ExceptionResponse(ex.getMessage()));
    }

    @ExceptionHandler({ConflictException.class, UsernameAlreadyExistsException.class, EmailAlreadyExistsException.class})
    public ResponseEntity<ExceptionResponse> handleConflict(RuntimeException ex, WebRequest request) {
        log.warn("Auth conflict {}", buildContext(request, errorAttributes, ex));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ExceptionResponse(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleGeneric(Exception ex, WebRequest request) {
        log.error("Auth unhandled error {}", buildContext(request, errorAttributes, ex), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ExceptionResponse("Internal server error"));
    }
}
