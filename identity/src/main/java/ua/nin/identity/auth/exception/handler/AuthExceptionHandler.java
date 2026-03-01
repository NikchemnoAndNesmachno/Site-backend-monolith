package ua.nin.identity.auth.exception.handler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import ua.nin.common.exception.response.ExceptionResponse;
import ua.nin.common.logging.ErrorLogContext;
import ua.nin.identity.auth.controller.AuthController;
import ua.nin.identity.auth.controller.EmailVerificationController;
import ua.nin.identity.auth.controller.PasswordController;
import ua.nin.identity.auth.exception.exceptions.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@AllArgsConstructor
@RestControllerAdvice(assignableTypes = {AuthController.class, PasswordController.class, EmailVerificationController.class})
public class AuthExceptionHandler {

    private final ErrorAttributes errorAttributes;

    private Map<String, Object> getErrorAttributes(WebRequest webRequest) {
        Map<String, Object> map = new HashMap<>(
                errorAttributes.getErrorAttributes(
                        webRequest,
                        ErrorAttributeOptions.of(
                                ErrorAttributeOptions.Include.MESSAGE,
                                ErrorAttributeOptions.Include.PATH
                        )
                )
        );

        if (webRequest instanceof ServletWebRequest servletWebRequest) {
            String path = servletWebRequest.getRequest().getMethod() + " "
                    + servletWebRequest.getRequest().getRequestURI();
            map.put("path", path);
        }

        return map;
    }

    private ErrorLogContext buildContext(WebRequest request, Exception ex) {
        return ErrorLogContext.from(getErrorAttributes(request), ex);
    }

    @ExceptionHandler({BadCredentialsException.class, InvalidTokenException.class, InvalidRefreshTokenException.class, MissingTokenException.class})
    public ResponseEntity<ExceptionResponse> handleBadRequest(RuntimeException ex, WebRequest request) {
        log.warn("Auth unauthorized {}", buildContext(request, ex), ex);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ExceptionResponse(ex.getMessage()));
    }

    @ExceptionHandler({ForbiddenException.class, RefreshReuseDetectedException.class, TokenAlreadyUsedException.class, TokenExpiredException.class})
    public ResponseEntity<ExceptionResponse> handleForbidden(RuntimeException ex, WebRequest request) {
        log.warn("Auth forbidden {}", buildContext(request, ex), ex);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ExceptionResponse(ex.getMessage()));
    }

    @ExceptionHandler({NotFoundException.class, UserNotFoundException.class, CredentialNotFoundException.class})
    public ResponseEntity<ExceptionResponse> handleNotFound(RuntimeException ex, WebRequest request) {
        log.warn("Auth not found {}", buildContext(request, ex), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ExceptionResponse(ex.getMessage()));
    }

    @ExceptionHandler({ConflictException.class, UsernameAlreadyExistsException.class, EmailAlreadyExistsException.class})
    public ResponseEntity<ExceptionResponse> handleConflict(RuntimeException ex, WebRequest request) {
        log.warn("Auth conflict {}", buildContext(request, ex), ex);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ExceptionResponse(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleGeneric(Exception ex, WebRequest request) {
        log.error("Auth unhandled error {}", buildContext(request, ex), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ExceptionResponse("Internal server error"));
    }
}
