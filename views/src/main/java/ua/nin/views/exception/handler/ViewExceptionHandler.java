package ua.nin.views.exception.handler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import ua.nin.common.exception.response.ExceptionResponse;
import ua.nin.views.controller.ViewController;
import ua.nin.views.exception.exceptions.ViewerKeyHashException;

import static ua.nin.common.exception.util.ExceptionHandlerUtils.buildContext;

@Slf4j
@AllArgsConstructor
@RestControllerAdvice(assignableTypes = ViewController.class)
public class ViewExceptionHandler {

    private final ErrorAttributes errorAttributes;

    @ExceptionHandler(ViewerKeyHashException.class)
    public ResponseEntity<ExceptionResponse> handleViewerKeyHashException(ViewerKeyHashException ex, WebRequest request) {
        log.warn("Views hash failure {}", buildContext(request, errorAttributes, ex));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ExceptionResponse("Internal server error"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleGeneric(Exception ex, WebRequest request) {
        log.error("Views unhandled error {}", buildContext(request, errorAttributes, ex), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ExceptionResponse("Internal server error"));
    }
}
