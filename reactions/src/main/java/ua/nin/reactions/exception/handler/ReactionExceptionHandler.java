package ua.nin.reactions.exception.handler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import ua.nin.common.exception.response.ExceptionResponse;
import ua.nin.reactions.controller.ReactionController;
import ua.nin.reactions.exception.exceptions.UnknownReactionTypeException;

import static ua.nin.common.exception.util.ExceptionHandlerUtils.buildContext;

@Slf4j
@AllArgsConstructor
@RestControllerAdvice(assignableTypes = ReactionController.class)
public class ReactionExceptionHandler {

    private final ErrorAttributes errorAttributes;

    @ExceptionHandler(UnknownReactionTypeException.class)
    public ResponseEntity<ExceptionResponse> handleBadRequest(RuntimeException ex, WebRequest request) {
        log.warn("Unknown reaction type {}", buildContext(request, errorAttributes, ex));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ExceptionResponse(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleGeneric(Exception ex, WebRequest request) {
        log.error("Reaction unhandled error {}", buildContext(request, errorAttributes, ex), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ExceptionResponse("Internal server error"));
    }
}
