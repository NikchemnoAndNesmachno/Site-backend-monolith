package ua.nin.common.logging;

import java.time.Instant;
import java.util.Date;
import java.util.Map;

public record ErrorLogContext(
        String requestId,
        String path,
        Instant timestamp,
        String exception,
        String message
) {
    public static ErrorLogContext from(Map<String, Object> attrs, Exception ex) {

        Instant timestamp = ((Date) attrs.get("timestamp")).toInstant();

        return new ErrorLogContext(
                (String) attrs.get("requestId"),
                (String) attrs.get("path"),
                timestamp,
                ex.getClass().getSimpleName(),
                ex.getMessage()
        );
    }
}