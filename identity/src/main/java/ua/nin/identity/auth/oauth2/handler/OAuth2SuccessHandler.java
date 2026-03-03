package ua.nin.identity.auth.oauth2.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import ua.nin.identity.auth.dto.AuthResponse;
import ua.nin.identity.auth.dto.IssueNewResult;
import ua.nin.identity.auth.dto.OAuth2UserDto;
import ua.nin.identity.auth.model.Provider;
import ua.nin.identity.auth.model.User;
import ua.nin.identity.auth.service.AccessTokenService;
import ua.nin.identity.auth.service.HttpCookieService;
import ua.nin.identity.auth.service.RefreshTokenService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final Oauth2ProvisionService provisionService;
    private final AccessTokenService accessTokenService;
    private final RefreshTokenService refreshTokenService;
    private final HttpCookieService cookieService;
    private final ObjectMapper objectMapper;

    @Value("${jwt.access-ttl-minutes:10}")
    private long accessTtlMinutes;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        // 1. Отримання провайдера
        String provider;
        OAuth2AuthenticationToken token  = (OAuth2AuthenticationToken) authentication;
        provider = token.getAuthorizedClientRegistrationId();
        //-------------------------------------- Other providers (GitHub for example) -----------------
//        if (authentication instanceof OAuth2AuthenticationToken token) {
//            provider = token.getAuthorizedClientRegistrationId();
//        }

        // 2. Отримання об'єкта Oidc і витягування даних користувача з нього
        // sub (providerId), email, emailVerified, name, picture (link)

//        DefaultOAuth2User principal = (DefaultOAuth2User) authentication.getPrincipal();
//        Integer providerId = principal.getAttribute("id");
//        String email = principal.getAttribute("email");
//        String name = principal.getAttribute("name");
//        String login = principal.getAttribute("login");
//        String picture = principal.getAttribute("avatar_url");
//-----------------------------------------------------------------------------------------------------------
        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();

        String providerId = oidcUser.getSubject();
        String email = oidcUser.getEmail();
        Boolean emailVerified = oidcUser.getEmailVerified();
        String name = oidcUser.getFullName();
        String picture = oidcUser.getPicture();

        OAuth2UserDto oAuth2UserDto = OAuth2UserDto.builder()
                .provider(Provider.GOOGLE)
                .providerId(providerId)
                .email(email)
                .emailVerified(emailVerified)
                .name(name)
                .picture(picture)
                .build();

        // 1) Створити/оновити користувача і прив'язку googleSub
        User user = provisionService.provision(oAuth2UserDto);

        //"User-Agent"
        String userAgent = request.getHeader(HttpHeaders.USER_AGENT);
        String ip = request.getRemoteAddr();
        // 2) Випустити власні токени
        // refresh issue (family + token)
        IssueNewResult refresh = refreshTokenService.issueNew(user, userAgent, ip);

        // access token
        String role = user.getRole().name();
        String access = accessTokenService.createAccessToken(user.getId(), List.of(role));

        AuthResponse responseDto = new AuthResponse(
                access,
                "Bearer",
                accessTtlMinutes * 60,
                user.getId(),
                role
        );

        cookieService.setRefreshCookie(response, refresh.rawRefreshToken());

        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        objectMapper.writeValue(response.getOutputStream(), responseDto);
        response.flushBuffer();
    }
}