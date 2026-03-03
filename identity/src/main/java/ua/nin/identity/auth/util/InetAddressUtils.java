package ua.nin.identity.auth.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.net.InetAddress;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class InetAddressUtils {

    public static InetAddress parseIp(String ip) {
        if (ip == null || ip.isBlank()) return null;

        String value = ip.trim();

        try {
            return InetAddress.getByName(value);
        } catch (Exception e) {
            return null;
        }
    }
}
