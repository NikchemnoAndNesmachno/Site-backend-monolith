package ua.nin.reactions.exception.handler;

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
import ua.nin.reactions.controller.ReactionController;
import ua.nin.reactions.exception.exceptions.UnknownReactionTypeException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@AllArgsConstructor
@RestControllerAdvice(assignableTypes = ReactionController.class)
public class ReactionExceptionHandler {

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

    @ExceptionHandler(UnknownReactionTypeException.class)
    public ResponseEntity<ExceptionResponse> handleUnknownReaction(UnknownReactionTypeException ex, WebRequest request) {
        log.warn("Unknown reaction type {}", buildContext(request, ex), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ExceptionResponse(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleGeneric(Exception ex, WebRequest request) {
        log.error("Reaction unhandled error {}", buildContext(request, ex), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ExceptionResponse("Internal server error"));
    }
}
