package ua.nin.contract.feed;

import java.util.Collection;
import java.util.Map;

public interface CommentStatsPort {
    Map<Long, Long> getCommentCountsByVideoIds(Collection<Long> videoIds);
}
