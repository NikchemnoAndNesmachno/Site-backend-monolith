package ua.nin.contract.feed.dto;

import java.util.List;

public record FeedVideoPage(
        List<FeedVideoBaseView> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {}
