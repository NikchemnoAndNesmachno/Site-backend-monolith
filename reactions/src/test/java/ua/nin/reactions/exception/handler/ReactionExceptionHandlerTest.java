package ua.nin.reactions.exception.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;
import ua.nin.common.exception.response.ExceptionResponse;
import ua.nin.reactions.exception.exceptions.UnknownReactionTypeException;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReactionExceptionHandlerTest {

    @Mock
    WebRequest webRequest;
    @Mock
    ErrorAttributes errorAttributes;
    @InjectMocks
    ReactionExceptionHandler handler;

    Map<String, Object> objectMap;

    @BeforeEach
    void init() {
        objectMap = new HashMap<>();
        objectMap.put("path", "/api/reactions");
        objectMap.put("message", "test");
        objectMap.put("timestamp", new Date());
        objectMap.put("trace", "Internal Server Error");
        when(errorAttributes.getErrorAttributes(eq(webRequest),
                any(ErrorAttributeOptions.class))).thenReturn(objectMap);
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put("requestId", requestId);
        MDC.put("path", "/api/reactions");
    }

    static Stream<Arguments> badRequestExceptions() {
        return Stream.of(
                Arguments.of(new UnknownReactionTypeException("unknown reaction type"))
        );
    }

    @ParameterizedTest
    @MethodSource("badRequestExceptions")
    void handleBadRequest(RuntimeException ex) {
        assertEquals(HttpStatus.BAD_REQUEST, handler.handleBadRequest(ex, webRequest).getStatusCode());
    }

    @Test
    void testHandleInternalServerErrorException() {
        RuntimeException exception = new RuntimeException("test");
        assertEquals(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ExceptionResponse("Internal server error")),
                handler.handleGeneric(exception, webRequest));
    }
}