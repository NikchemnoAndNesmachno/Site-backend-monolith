package ua.nin.comments.exception.handler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import ua.nin.comments.controller.CommentController;
import ua.nin.comments.exception.exceptions.BadRequestException;
import ua.nin.comments.exception.exceptions.ForbiddenException;
import ua.nin.comments.exception.exceptions.NotFoundException;
import ua.nin.common.exception.response.ExceptionResponse;

import static ua.nin.common.exception.util.ExceptionHandlerUtils.buildContext;

@Slf4j
@AllArgsConstructor
@RestControllerAdvice(assignableTypes = CommentController.class)
public class CommentExceptionHandler {

    private final ErrorAttributes errorAttributes;

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleNotFound(RuntimeException ex, WebRequest request) {
        log.warn("Comment not found {}", buildContext(request, errorAttributes, ex));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ExceptionResponse(ex.getMessage()));
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ExceptionResponse> handleForbidden(RuntimeException ex, WebRequest request) {
        log.warn("Comment forbidden {}", buildContext(request, errorAttributes, ex));
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ExceptionResponse(ex.getMessage()));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ExceptionResponse> handleBadRequest(RuntimeException ex, WebRequest request) {
        log.warn("Comment bad request {}", buildContext(request, errorAttributes, ex));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ExceptionResponse(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleGeneric(Exception ex, WebRequest request) {
        log.warn("Comment unhandled error {}", buildContext(request, errorAttributes, ex), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ExceptionResponse("Internal server error"));
    }
}
