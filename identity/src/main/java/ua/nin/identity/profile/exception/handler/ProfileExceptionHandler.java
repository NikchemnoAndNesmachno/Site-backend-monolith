package ua.nin.identity.profile.exception.handler;

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
import ua.nin.identity.profile.controller.ProfileController;
import ua.nin.identity.profile.exception.exceptions.ProfileNotFoundException;
import ua.nin.identity.profile.exception.exceptions.UsernameAlreadyTakenException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@AllArgsConstructor
@RestControllerAdvice(assignableTypes = ProfileController.class)
public class ProfileExceptionHandler {

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

    @ExceptionHandler(ProfileNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleNotFound(ProfileNotFoundException ex, WebRequest request) {
        log.warn("Profile not found {}", buildContext(request, ex), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ExceptionResponse(ex.getMessage()));
    }

    @ExceptionHandler(UsernameAlreadyTakenException.class)
    public ResponseEntity<ExceptionResponse> handleConflict(UsernameAlreadyTakenException ex, WebRequest request) {
        log.warn("Username already taken {}", buildContext(request, ex), ex);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ExceptionResponse(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleGeneric(Exception ex, WebRequest request) {
        log.error("Profile unhandled error {}", buildContext(request, ex), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ExceptionResponse("Internal server error"));
    }
}
