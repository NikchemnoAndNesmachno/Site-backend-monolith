import type {FeedSort, PageResponse, VideoListItem} from "../types/video.ts";
import {axiosPrivate} from "./axios.ts";

export type GetVideosPageParams = {
    page: number;
    size: number;
    sort: FeedSort;
};

export async function getVideosPage(params: GetVideosPageParams): Promise<PageResponse<VideoListItem>> {
    const response = await axiosPrivate.get<PageResponse<VideoListItem>>("/api/v1/feed", {
        params: {
            page: params.page,
            size: params.size,
            sort: params.sort,
        },
    });

    return response.data;
}