package ua.nin.media.exception.handler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import ua.nin.common.exception.response.ExceptionResponse;
import ua.nin.media.controller.MediaController;
import ua.nin.media.controller.UserAvatarController;
import ua.nin.media.controller.VideoController;
import ua.nin.media.exception.exceptions.*;

import static ua.nin.common.exception.util.ExceptionHandlerUtils.buildContext;

@Slf4j
@AllArgsConstructor
@RestControllerAdvice(assignableTypes = {MediaController.class, VideoController.class, UserAvatarController.class})
public class MediaExceptionHandler {

    private final ErrorAttributes errorAttributes;

    @ExceptionHandler({MediaNotFoundException.class, FileNotFoundException.class, AvatarNotFoundException.class, VideoNotFound.class})
    public ResponseEntity<ExceptionResponse> handleNotFound(RuntimeException ex, WebRequest request) {
        log.warn("Media not found {}", buildContext(request, errorAttributes, ex));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ExceptionResponse(ex.getMessage()));
    }

    @ExceptionHandler({MediaValidationException.class})
    public ResponseEntity<ExceptionResponse> handleBadRequest(RuntimeException ex, WebRequest request) {
        log.warn("Media bad request {}", buildContext(request, errorAttributes, ex));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ExceptionResponse(ex.getMessage()));
    }

    @ExceptionHandler({AvatarForbiddenDeletionException.class, VideoForbiddenDeletionException.class})
    public ResponseEntity<ExceptionResponse> handleForbidden(RuntimeException ex, WebRequest request) {
        log.warn("Media forbidden {}", buildContext(request, errorAttributes, ex));
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ExceptionResponse(ex.getMessage()));
    }

    @ExceptionHandler({FileMovingException.class, FileStoringException.class})
    public ResponseEntity<ExceptionResponse> handleStorageErrors(RuntimeException ex, WebRequest request) {
        log.warn("Media storage error {}", buildContext(request, errorAttributes, ex));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ExceptionResponse(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleGeneric(Exception ex, WebRequest request) {
        log.error("Media unhandled error {}", buildContext(request, errorAttributes, ex), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ExceptionResponse("Internal server error"));
    }
}
