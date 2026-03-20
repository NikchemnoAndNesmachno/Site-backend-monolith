import { useQuery } from "@tanstack/react-query";
import {getVideosPage} from "../api/feed.api.ts";
import type {FeedSort} from "../types/video.ts";

export function useVideosPage(page: number, size: number, sort: FeedSort) {
    return useQuery({
        queryKey: ["videos", "page", page, "size", size, "sort", sort],
        queryFn: () => getVideosPage({ page, size, sort }),
        staleTime: 60_000,
        placeholderData: (previousData) => previousData,
    });
}