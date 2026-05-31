package ua.nin.contract.feed;

import java.util.Collection;
import java.util.Map;

public interface ViewStatsPort {
    Map<Long, Long> getViewCountsByVideoIds(Collection<Long> videoIds);
}
