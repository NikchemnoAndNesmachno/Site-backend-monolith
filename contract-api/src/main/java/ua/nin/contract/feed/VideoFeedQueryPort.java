package ua.nin.contract.feed;

import ua.nin.contract.feed.dto.FeedVideoPage;

public interface VideoFeedQueryPort {
    FeedVideoPage findPublicVideoPage(int page, int size, FeedSort sort);
}
