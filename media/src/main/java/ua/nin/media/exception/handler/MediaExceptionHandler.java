package ua.nin.media.exception.handler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import ua.nin.common.exception.response.ExceptionResponse;
import ua.nin.common.logging.ErrorLogContext;
import ua.nin.media.controller.MediaController;
import ua.nin.media.controller.UserAvatarController;
import ua.nin.media.controller.VideoController;
import ua.nin.media.exception.exceptions.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@AllArgsConstructor
@RestControllerAdvice(assignableTypes = {MediaController.class, VideoController.class, UserAvatarController.class})
public class MediaExceptionHandler {

    private final ErrorAttributes errorAttributes;

    private Map<String, Object> getErrorAttributes(WebRequest webRequest) {
        return new HashMap<>(errorAttributes.getErrorAttributes(webRequest,
                ErrorAttributeOptions.of(ErrorAttributeOptions.Include.MESSAGE)));
    }

    private ErrorLogContext buildContext(WebRequest request, Exception ex) {
        return ErrorLogContext.from(getErrorAttributes(request), ex);
    }

    @ExceptionHandler({MediaNotFoundException.class, AvatarNotFoundException.class, VideoNotFound.class})
    public ResponseEntity<ExceptionResponse> handleNotFound(RuntimeException ex, WebRequest request) {
        log.warn("Media not found {}", buildContext(request, ex), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ExceptionResponse(ex.getMessage()));
    }

    @ExceptionHandler({FileMovingException.class, FileStoringException.class})
    public ResponseEntity<ExceptionResponse> handleStorageErrors(RuntimeException ex, WebRequest request) {
        log.warn("Media storage error {}", buildContext(request, ex), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ExceptionResponse(ex.getMessage()));
    }

    @ExceptionHandler({AvatarForbiddenDeletionException.class, VideoForbiddenDeletionException.class, MediaValidationException.class})
    public ResponseEntity<ExceptionResponse> handleBadRequest(RuntimeException ex, WebRequest request) {
        log.warn("Media request error {}", buildContext(request, ex), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ExceptionResponse(ex.getMessage()));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ExceptionResponse> handleResponseStatus(ResponseStatusException ex, WebRequest request) {
        log.warn("Media response status {}", buildContext(request, ex), ex);
        return ResponseEntity.status(ex.getStatusCode()).body(new ExceptionResponse(ex.getReason()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleGeneric(Exception ex, WebRequest request) {
        log.error("Media unhandled error {}", buildContext(request, ex), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ExceptionResponse("Internal server error"));
    }
}
