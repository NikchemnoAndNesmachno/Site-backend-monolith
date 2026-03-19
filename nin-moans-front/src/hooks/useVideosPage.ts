import { useQuery } from "@tanstack/react-query";
import {getVideosPage} from "../api/feed.api.ts";

export function useVideosPage(page: number, size: number) {
    return useQuery({
        queryKey: ["videos", "page", page, "size", size],
        queryFn: () => getVideosPage({ page, size }),
        staleTime: 60_000,
        placeholderData: (previousData) => previousData,
    });
}