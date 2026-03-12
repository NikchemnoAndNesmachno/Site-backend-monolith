package ua.nin.feed.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.nin.contract.feed.CommentStatsPort;
import ua.nin.contract.feed.FeedSort;
import ua.nin.contract.feed.dto.FeedVideoBaseView;
import ua.nin.contract.feed.dto.FeedVideoPage;
import ua.nin.contract.feed.ReactionStatsPort;
import ua.nin.contract.feed.VideoFeedQueryPort;
import ua.nin.contract.feed.ViewStatsPort;
import ua.nin.feed.dto.FeedAuthorResponse;
import ua.nin.feed.dto.FeedPageResponse;
import ua.nin.feed.dto.FeedVideoItemResponse;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedService {

    private static final String LIKE = "LIKE";
    private static final String DISLIKE = "DISLIKE";

    private final VideoFeedQueryPort videoFeedQueryPort;
    private final ReactionStatsPort reactionStatsPort;
    private final CommentStatsPort commentStatsPort;
    private final ViewStatsPort viewStatsPort;

    public FeedPageResponse getFeed(int page, int size, FeedSort sort, Long currentUserId) {
        FeedVideoPage videoPage = videoFeedQueryPort.findPublicVideoPage(page, size, sort);

        if (videoPage.items().isEmpty()) {
            return new FeedPageResponse(
                    List.of(),
                    videoPage.page(),
                    videoPage.size(),
                    videoPage.totalElements(),
                    videoPage.totalPages(),
                    videoPage.hasNext()
            );
        }

        List<Long> videoIds = videoPage.items().stream()
                .map(FeedVideoBaseView::videoId)
                .toList();

        Map<Long, Long> likesByVideoId = reactionStatsPort.getReactionCountsByVideoIds(videoIds, LIKE);
        Map<Long, Long> dislikesByVideoId = reactionStatsPort.getReactionCountsByVideoIds(videoIds, DISLIKE);
        Map<Long, Long> commentsByVideoId = commentStatsPort.getCommentCountsByVideoIds(videoIds);
        Map<Long, Long> viewsByVideoId = viewStatsPort.getViewCountsByVideoIds(videoIds);

        Map<Long, String> myReactionsByVideoId = currentUserId == null
                ? Map.of()
                : reactionStatsPort.getMyReactionCodesForVideoIds(currentUserId, videoIds);

        List<FeedVideoItemResponse> items = videoPage.items().stream()
                .map(video -> toFeedVideoItemResponse(
                        video,
                        likesByVideoId,
                        dislikesByVideoId,
                        commentsByVideoId,
                        viewsByVideoId,
                        myReactionsByVideoId
                ))
                .toList();

        return new FeedPageResponse(
                items,
                videoPage.page(),
                videoPage.size(),
                videoPage.totalElements(),
                videoPage.totalPages(),
                videoPage.hasNext()
        );
    }

    private FeedVideoItemResponse toFeedVideoItemResponse(
            FeedVideoBaseView video,
            Map<Long, Long> likesByVideoId,
            Map<Long, Long> dislikesByVideoId,
            Map<Long, Long> commentsByVideoId,
            Map<Long, Long> viewsByVideoId,
            Map<Long, String> myReactionsByVideoId
    ) {
        Long videoId = video.videoId();

        return new FeedVideoItemResponse(
                videoId,
                video.title(),
                video.description(),
                video.previewMediaId(),
                buildMediaUrl(video.previewMediaId()),
                new FeedAuthorResponse(
                        video.ownerUserId(),
                        video.ownerUsername(),
                        video.ownerDisplayName(),
                        video.ownerAvatarMediaId(),
                        buildMediaUrl(video.ownerAvatarMediaId())
                ),
                viewsByVideoId.getOrDefault(videoId, 0L),
                likesByVideoId.getOrDefault(videoId, 0L),
                dislikesByVideoId.getOrDefault(videoId, 0L),
                commentsByVideoId.getOrDefault(videoId, 0L),
                myReactionsByVideoId.get(videoId),
                video.createdAt()
        );
    }

    /**
     * Якщо не хочеш будувати URL на бекенді — прибери це і залиш лише mediaId.
     */
    private String buildMediaUrl(Long mediaId) {
        return mediaId == null ? null : "/api/v1/media/" + mediaId;
    }
}