package ua.nin.identity.auth.oauth2.state;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.util.StringUtils;
import ua.nin.identity.auth.exception.exceptions.CookieDeserializeException;
import ua.nin.identity.auth.exception.exceptions.CookieSerializeException;
import ua.nin.identity.auth.service.HttpCookieService;
import ua.nin.identity.auth.util.TimeTokenUtils;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

@RequiredArgsConstructor
public class CookieAuthorizationRequestRepository implements
        AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    public static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";
    public static final String REDIRECT_URI_COOKIE_NAME = "redirect_uri";
    public static final String OAUTH2_PATH = "/";

    private static final int COOKIE_MAX_AGE_SECONDS = 180;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final TimeTokenUtils timeTokenUtils;
    private final HttpCookieService httpCookieService;

    @Override
    public void saveAuthorizationRequest(
            OAuth2AuthorizationRequest authorizationRequest,
            HttpServletRequest request,
            HttpServletResponse response) {
        if (authorizationRequest == null) {
            removeAuthorizationRequestCookies(response);
            return;
        }

        String serializedRequest = serialize(authorizationRequest);
        httpCookieService.addCookie(response,
                OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME,
                serializedRequest,
                OAUTH2_PATH,
                COOKIE_MAX_AGE_SECONDS
        );

        String redirectUri = (String) authorizationRequest.getAdditionalParameters()
                .get(REDIRECT_URI_COOKIE_NAME);

        if (StringUtils.hasText(redirectUri)) {
            httpCookieService.addCookie(response, REDIRECT_URI_COOKIE_NAME, redirectUri, OAUTH2_PATH, COOKIE_MAX_AGE_SECONDS);
        }
    }

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return httpCookieService.getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
                .map(Cookie::getValue)
                .map(cookieValue -> deserialize(cookieValue, OAuth2AuthorizationRequest.class))
                .orElse(null);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(
            HttpServletRequest request,
            HttpServletResponse response) {
        OAuth2AuthorizationRequest authorizationRequest = loadAuthorizationRequest(request);

        removeAuthorizationRequestCookies(response);

        return authorizationRequest;
    }

    public void removeAuthorizationRequestCookies(HttpServletResponse response) {
        httpCookieService.deleteCookie(response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, OAUTH2_PATH);
        httpCookieService.deleteCookie(response, REDIRECT_URI_COOKIE_NAME, OAUTH2_PATH);
    }

    public String serialize(Serializable object) {
        try {
            OAuth2AuthorizationRequest o = (OAuth2AuthorizationRequest) object;
            String json = MAPPER.writeValueAsString(o);
            String data = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(json.getBytes(StandardCharsets.UTF_8));

            String sig = timeTokenUtils.hmacSha256(data);
            return data + "." + sig;
        } catch (Exception e) {
            throw new CookieSerializeException("Serialization failed.", e);
        }
    }

    public <T> T deserialize(String value, Class<T> cls) {
        try {
            if (value == null || !value.contains(".")) {
                throw new CookieDeserializeException("Invalid cookie format");
            }

            String[] parts = value.split("\\.");
            if (parts.length != 2) {
                throw new CookieDeserializeException("Invalid cookie structure");
            }

            String data = parts[0];
            String sentSig = parts[1];

            // 1. Перевіряємо підпис
            String expectedSig = timeTokenUtils.hmacSha256(data);

            if (!MessageDigest.isEqual(
                    expectedSig.getBytes(StandardCharsets.UTF_8),
                    sentSig.getBytes(StandardCharsets.UTF_8))) {
                throw new CookieDeserializeException("Invalid HMAC signature");
            }

            // 2. Декодуємо payload
            byte[] decoded = Base64.getUrlDecoder().decode(data);
            Map<String, Object> p = MAPPER.readValue(decoded, new TypeReference<>() {});

            @SuppressWarnings("unchecked")
            OAuth2AuthorizationRequest result =
                    OAuth2AuthorizationRequest.authorizationCode()
                            .authorizationUri((String) p.get("authorizationUri"))
                            .clientId((String) p.get("clientId"))
                            .redirectUri((String) p.get("redirectUri"))
                            .scopes(new HashSet<>((List<String>) p.get("scopes")))
                            .state((String) p.get("state"))
                            .attributes((Map<String, Object>) p.getOrDefault("attributes", Map.of()))
                            .additionalParameters((Map<String, Object>) p.getOrDefault("additionalParameters", Map.of()))
                            .build();

            if (!cls.isInstance(result)) {
                throw new CookieDeserializeException("Unexpected deserialized type: " + result.getClass(), null);
            }
            return cls.cast(result);
        } catch (Exception e) {
            throw new CookieDeserializeException("Deserialization failed", e);
        }
    }
}