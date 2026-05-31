package ua.nin.contract.feed;

import java.util.Collection;
import java.util.Map;

public interface ReactionStatsPort {

    /**
     * Наприклад для LIKE:
     * key = videoId
     * value = count
     */
    Map<Long, Long> getReactionCountsByVideoIds(Collection<Long> videoIds, String reactionCode);

    /**
     * key = videoId
     * value = reactionCode користувача (LIKE / DISLIKE / etc)
     */
    Map<Long, String> getMyReactionCodesForVideoIds(long userId, Collection<Long> videoIds);
}