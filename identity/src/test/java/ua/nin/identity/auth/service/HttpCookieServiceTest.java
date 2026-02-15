package ua.nin.identity.auth.service;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

class HttpCookieServiceTest {

    @Test
    void setRefreshCookie_writesHttpOnlyCookie() {
        HttpCookieService service = new HttpCookieService(14L, false);
        MockHttpServletResponse response = new MockHttpServletResponse();

        service.setRefreshCookie(response, "token");

        String header = response.getHeader(HttpHeaders.SET_COOKIE);
        assertNotNull(header);
        assertTrue(header.contains("refresh_token=token"));
        assertTrue(header.contains("HttpOnly"));
        assertTrue(header.contains("Max-Age"));
        assertTrue(header.contains("Path=/api/v1/auth/refresh"));
    }

    @Test
    void clearRefreshCookie_setsExpiredCookie() {
        HttpCookieService service = new HttpCookieService(14L, false);
        MockHttpServletResponse response = new MockHttpServletResponse();

        service.clearRefreshCookie(response);

        String header = response.getHeader(HttpHeaders.SET_COOKIE);
        assertNotNull(header);
        assertTrue(header.contains("refresh_token="));
        assertTrue(header.contains("Max-Age=0"));
    }
}
