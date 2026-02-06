package ua.nin.identity.auth.util;

import org.junit.jupiter.api.Test;

import java.net.InetAddress;

import static org.junit.jupiter.api.Assertions.*;

class InetAddressUtilsTest {

    @Test
    void parseIp_nullOrBlank_returnsNull() {
        assertNull(InetAddressUtils.parseIp(null));
        assertNull(InetAddressUtils.parseIp(" "));
    }

    @Test
    void parseIp_invalid_returnsNull() {
        assertNull(InetAddressUtils.parseIp("not-an-ip"));
    }

    @Test
    void parseIp_valid_returnsInetAddress() {
        InetAddress address = InetAddressUtils.parseIp("127.0.0.1");

        assertNotNull(address);
        assertEquals("127.0.0.1", address.getHostAddress());
    }
}
