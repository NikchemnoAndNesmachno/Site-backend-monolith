package ua.nin.views.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.nin.views.dto.ViewCountsResponse;
import ua.nin.views.mapper.ViewCountsResponseMapper;
import ua.nin.views.model.ViewCount;
import ua.nin.views.repository.ViewCountRepository;
import ua.nin.views.repository.ViewUniqueRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static ua.nin.common.util.StringHelperUtils.normalizeTargetType;

/*
hashing viewer key

Для logged-in:
viewer key = "u:" + userId

Для guest:
viewer key = "g:" + ip + "|" + userAgent (і обов'язково pepper/salt)
 */
@Service
@RequiredArgsConstructor
public class ViewService {

    private final ViewUniqueRepository uniqueRepo;
    private final ViewCountRepository countRepo;
    private final ViewCountsResponseMapper viewCountsResponseMapper;

    @Value("${views.unique.bucket:DAY}") // DAY only for MVP
    private String bucketMode;

    @Value("${views.viewer.pepper:CHANGE_ME}")
    private String pepper;

    @Transactional
    public void recordView(String targetType, long targetId, Long userId, String userAgent, String ip) {
        Instant now = Instant.now();

        String tType = normalizeTargetType(targetType);
        Instant bucketStart = bucketStartDayUtc(now);

        String viewerKey = buildViewerKey(userId, userAgent, ip);
        String viewerHash = sha256Hex(viewerKey + "." + pepper);

        int inserted = uniqueRepo.insertUniqueIfAbsent(tType, targetId, viewerHash, bucketStart, now);
        long uniqueInc = inserted == 1 ? 1 : 0;

        countRepo.upsertIncrement(tType, targetId, 1, uniqueInc);
    }

    @Transactional(readOnly = true)
    public ViewCountsResponse getCounts(String targetType, long targetId) {
        String tType = normalizeTargetType(targetType);

        ViewCount viewCount = countRepo.findCountsByTarget(tType, targetId);

        return viewCountsResponseMapper.toDto(viewCount);
    }

    private static Instant bucketStartDayUtc(Instant now) {
        ZonedDateTime z = now.atZone(ZoneOffset.UTC);
        return z.toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant();
    }

    private static String buildViewerKey(Long userId, String userAgent, String ip) {
        if (userId != null) return "u:" + userId;
        String ua = userAgent == null ? "" : userAgent.trim();
        String addr = ip == null ? "" : ip.trim();
        return "g:" + addr + "|" + ua;
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot hash viewer key", e);
        }
    }
}
