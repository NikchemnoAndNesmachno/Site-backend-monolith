package ua.nin.feed.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.nin.contract.feed.CommentStatsPort;
import ua.nin.contract.feed.FeedSort;
import ua.nin.contract.feed.ReactionStatsPort;
import ua.nin.contract.feed.VideoFeedQueryPort;
import ua.nin.contract.feed.ViewStatsPort;
import ua.nin.contract.feed.dto.FeedVideoBaseView;
import ua.nin.contract.feed.dto.FeedVideoPage;
import ua.nin.feed.dto.FeedPageResponse;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeedServiceTest {

    @Mock
    private VideoFeedQueryPort videoFeedQueryPort;
    @Mock
    private ReactionStatsPort reactionStatsPort;
    @Mock
    private CommentStatsPort commentStatsPort;
    @Mock
    private ViewStatsPort viewStatsPort;

    @InjectMocks
    private FeedService feedService;

    @Test
    void getFeed_mapsStatsUrlsAndCurrentUserReaction() {
        FeedVideoBaseView firstVideo = new FeedVideoBaseView(
                11L,
                "First title",
                "First description",
                101L,
                "author-one",
                "Author One",
                201L,
                301L,
                Instant.parse("2026-03-20T12:00:00Z")
        );
        FeedVideoBaseView secondVideo = new FeedVideoBaseView(
                12L,
                "Second title",
                "Second description",
                102L,
                "author-two",
                "Author Two",
                null,
                null,
                Instant.parse("2026-03-21T12:00:00Z")
        );
        FeedVideoPage page = new FeedVideoPage(List.of(firstVideo, secondVideo), 1, 2, 10L, 5, true, true);

        when(videoFeedQueryPort.findPublicVideoPage(1, 2, FeedSort.POPULAR)).thenReturn(page);
        when(reactionStatsPort.getReactionCountsByVideoIds(List.of(11L, 12L), "LIKE"))
                .thenReturn(Map.of(11L, 7L));
        when(reactionStatsPort.getReactionCountsByVideoIds(List.of(11L, 12L), "DISLIKE"))
                .thenReturn(Map.of(12L, 2L));
        when(commentStatsPort.getCommentCountsByVideoIds(List.of(11L, 12L)))
                .thenReturn(Map.of(11L, 4L, 12L, 1L));
        when(viewStatsPort.getViewCountsByVideoIds(List.of(11L, 12L)))
                .thenReturn(Map.of(11L, 100L));
        when(reactionStatsPort.getMyReactionCodesForVideoIds(99L, List.of(11L, 12L)))
                .thenReturn(Map.of(11L, "LIKE"));

        FeedPageResponse response = feedService.getFeed(1, 2, FeedSort.POPULAR, 99L);

        assertThat(response.items()).hasSize(2);
        assertThat(response.page()).isEqualTo(1);
        assertThat(response.items().get(0).previewUrl()).isEqualTo("/api/v1/media/301");
        assertThat(response.items().get(0).author().avatarUrl()).isEqualTo("/api/v1/media/201");
        assertThat(response.items().get(0).viewsCount()).isEqualTo(100L);
        assertThat(response.items().get(0).likesCount()).isEqualTo(7L);
        assertThat(response.items().get(0).dislikesCount()).isZero();
        assertThat(response.items().get(0).commentsCount()).isEqualTo(4L);
        assertThat(response.items().get(0).myReaction()).isEqualTo("LIKE");
        assertThat(response.items().get(1).previewUrl()).isNull();
        assertThat(response.items().get(1).author().avatarUrl()).isNull();
        assertThat(response.items().get(1).viewsCount()).isZero();
        assertThat(response.items().get(1).likesCount()).isZero();
        assertThat(response.items().get(1).dislikesCount()).isEqualTo(2L);
        assertThat(response.items().get(1).commentsCount()).isEqualTo(1L);
        assertThat(response.items().get(1).myReaction()).isNull();
    }

    @Test
    void getFeed_returnsEmptyPageWithoutLoadingStatsForNoItems() {
        FeedVideoPage emptyPage = new FeedVideoPage(List.of(), 0, 8, 0L, 0, false, false);
        when(videoFeedQueryPort.findPublicVideoPage(0, 8, FeedSort.LATEST)).thenReturn(emptyPage);

        FeedPageResponse response = feedService.getFeed(0, 8, FeedSort.LATEST, 5L);

        assertThat(response.items()).isEmpty();
        assertThat(response.page()).isZero();
        assertThat(response.size()).isEqualTo(8);
        assertThat(response.hasNext()).isFalse();
        assertThat(response.hasPrevious()).isFalse();
        verify(reactionStatsPort, never()).getReactionCountsByVideoIds(org.mockito.ArgumentMatchers.anyList(), org.mockito.ArgumentMatchers.anyString());
        verify(reactionStatsPort, never()).getMyReactionCodesForVideoIds(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyList());
        verify(commentStatsPort, never()).getCommentCountsByVideoIds(org.mockito.ArgumentMatchers.anyList());
        verify(viewStatsPort, never()).getViewCountsByVideoIds(org.mockito.ArgumentMatchers.anyList());
    }

    @Test
    void getFeed_skipsCurrentUserReactionsForAnonymousUser() {
        FeedVideoBaseView video = new FeedVideoBaseView(
                21L,
                "Title",
                "Description",
                301L,
                "author",
                "Author",
                401L,
                501L,
                Instant.parse("2026-03-22T12:00:00Z")
        );
        FeedVideoPage page = new FeedVideoPage(List.of(video), 0, 1, 1L, 1, false, false);

        when(videoFeedQueryPort.findPublicVideoPage(0, 1, FeedSort.LATEST)).thenReturn(page);
        when(reactionStatsPort.getReactionCountsByVideoIds(List.of(21L), "LIKE")).thenReturn(Map.of());
        when(reactionStatsPort.getReactionCountsByVideoIds(List.of(21L), "DISLIKE")).thenReturn(Map.of());
        when(commentStatsPort.getCommentCountsByVideoIds(List.of(21L))).thenReturn(Map.of());
        when(viewStatsPort.getViewCountsByVideoIds(List.of(21L))).thenReturn(Map.of(21L, 5L));

        FeedPageResponse response = feedService.getFeed(0, 1, FeedSort.LATEST, null);

        assertThat(response.items()).singleElement().satisfies(item -> {
            assertThat(item.myReaction()).isNull();
            assertThat(item.viewsCount()).isEqualTo(5L);
        });
        verify(reactionStatsPort, never()).getMyReactionCodesForVideoIds(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyList());
    }
}