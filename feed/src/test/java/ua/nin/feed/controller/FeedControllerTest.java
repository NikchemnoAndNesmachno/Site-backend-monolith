package ua.nin.feed.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ua.nin.contract.feed.FeedSort;
import ua.nin.feed.dto.FeedAuthorResponse;
import ua.nin.feed.dto.FeedPageResponse;
import ua.nin.feed.dto.FeedVideoItemResponse;
import ua.nin.feed.service.FeedService;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FeedControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FeedService feedService;

    @InjectMocks
    private FeedController feedController;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(feedController).build();
    }

    @Test
    void getFeed_returnsResponseForAuthenticatedUser() throws Exception {
        FeedPageResponse response = new FeedPageResponse(
                List.of(new FeedVideoItemResponse(
                        11L,
                        "Title",
                        "Description",
                        12L,
                        "/api/v1/media/12",
                        new FeedAuthorResponse(7L, "author", "Author", 13L, "/api/v1/media/13"),
                        99L,
                        15L,
                        1L,
                        3L,
                        "LIKE",
                        Instant.parse("2026-03-20T12:00:00Z")
                )),
                2,
                4,
                100L,
                25,
                true,
                true
        );
        when(feedService.getFeed(2, 4, FeedSort.POPULAR, 5L)).thenReturn(response);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("5");
        when(authentication.isAuthenticated()).thenReturn(true);

        mockMvc.perform(get("/api/v1/feed")
                        .param("page", "2")
                        .param("size", "4")
                        .param("sort", "POPULAR")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].videoId").value(11))
                .andExpect(jsonPath("$.items[0].previewUrl").value("/api/v1/media/12"))
                .andExpect(jsonPath("$.items[0].author.username").value("author"))
                .andExpect(jsonPath("$.items[0].myReaction").value("LIKE"))
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.hasPrevious").value(true));

        verify(feedService).getFeed(2, 4, FeedSort.POPULAR, 5L);
    }

    @Test
    void getFeed_usesDefaultsAndAnonymousUserWhenAuthenticationMissing() throws Exception {
        FeedPageResponse response = new FeedPageResponse(List.of(), 0, 8, 0L, 0, false, false);
        when(feedService.getFeed(0, 8, FeedSort.LATEST, null)).thenReturn(response);

        mockMvc.perform(get("/api/v1/feed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items").isEmpty())
                .andExpect(jsonPath("$.size").value(8));

        verify(feedService).getFeed(0, 8, FeedSort.LATEST, null);
    }

    @Test
    void getFeed_ignoresInvalidAuthenticationName() throws Exception {
        FeedPageResponse response = new FeedPageResponse(List.of(), 1, 3, 0L, 0, false, true);
        when(feedService.getFeed(1, 3, FeedSort.LATEST, null)).thenReturn(response);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("not-a-number");
        when(authentication.isAuthenticated()).thenReturn(true);

        mockMvc.perform(get("/api/v1/feed")
                        .param("page", "1")
                        .param("size", "3")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.hasPrevious").value(true));

        verify(feedService).getFeed(1, 3, FeedSort.LATEST, null);
    }
}
