package ua.nin.identity.profile.exception.handler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import ua.nin.common.exception.response.ExceptionResponse;
import ua.nin.identity.profile.controller.ProfileController;
import ua.nin.identity.profile.exception.exceptions.ProfileNotFoundException;
import ua.nin.identity.profile.exception.exceptions.UsernameAlreadyTakenException;

import static ua.nin.common.exception.util.ExceptionHandlerUtils.buildContext;

@Slf4j
@AllArgsConstructor
@RestControllerAdvice(assignableTypes = ProfileController.class)
public class ProfileExceptionHandler {

    private final ErrorAttributes errorAttributes;

    @ExceptionHandler(ProfileNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleNotFound(RuntimeException ex, WebRequest request) {
        log.warn("Profile not found {}", buildContext(request, errorAttributes, ex));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ExceptionResponse(ex.getMessage()));
    }

    @ExceptionHandler(UsernameAlreadyTakenException.class)
    public ResponseEntity<ExceptionResponse> handleConflict(RuntimeException ex, WebRequest request) {
        log.warn("Username already taken {}", buildContext(request, errorAttributes, ex));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ExceptionResponse(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleGeneric(Exception ex, WebRequest request) {
        log.error("Profile unhandled error {}", buildContext(request, errorAttributes, ex), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ExceptionResponse("Internal server error"));
    }
}
