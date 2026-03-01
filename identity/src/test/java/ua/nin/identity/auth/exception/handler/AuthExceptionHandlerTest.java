package ua.nin.identity.auth.exception.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;
import ua.nin.common.exception.response.ExceptionResponse;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthExceptionHandlerTest {

    @Mock
    WebRequest webRequest;
    @Mock
    ErrorAttributes errorAttributes;
    @InjectMocks
    AuthExceptionHandler handler;

    Map<String, Object> objectMap;

    @BeforeEach
    void init() {
        objectMap = new HashMap<>();
        objectMap.put("path", "/api/auth");
        objectMap.put("message", "test");
        objectMap.put("timestamp", new Date());
        objectMap.put("trace", "Internal Server Error");
    }

    @Test
    void testHandleInternalServerErrorException() {
        RuntimeException exception = new RuntimeException("test");
        when(errorAttributes.getErrorAttributes(eq(webRequest),
                any(ErrorAttributeOptions.class))).thenReturn(objectMap);
        assertEquals(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ExceptionResponse("Internal server error")),
                handler.handleGeneric(exception, webRequest));
    }
}
