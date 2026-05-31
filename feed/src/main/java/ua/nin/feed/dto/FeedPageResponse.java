package ua.nin.feed.dto;

import java.util.List;

public record FeedPageResponse(
        List<FeedVideoItemResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {}