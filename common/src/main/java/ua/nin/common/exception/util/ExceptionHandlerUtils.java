package ua.nin.common.exception.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.slf4j.MDC;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.web.context.request.WebRequest;
import ua.nin.common.logging.ErrorLogContext;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExceptionHandlerUtils {

    public static Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributes errorAttributes) {
        Map<String, Object> map = new HashMap<>(
                errorAttributes.getErrorAttributes(
                        webRequest,
                        ErrorAttributeOptions.of(
                                ErrorAttributeOptions.Include.MESSAGE,
                                ErrorAttributeOptions.Include.PATH
                        )
                )
        );

        map.put("requestId", MDC.get("requestId"));
        map.put("path", MDC.get("method") + " " + MDC.get("path"));

        return map;
    }

    public static ErrorLogContext buildContext(WebRequest request, ErrorAttributes errorAttributes, Exception ex) {
        return ErrorLogContext.from(getErrorAttributes(request, errorAttributes), ex);
    }
}
