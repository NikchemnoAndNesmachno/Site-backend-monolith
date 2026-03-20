import axios, { axiosPrivate } from "./axios.ts";
import type {
    ReactionActionResponse,
    ReactionCode,
    ReactionCountsResponse,
    VideoDetails,
    ViewCountsResponse,
} from "../types/video.ts";

export async function getVideoDetails(videoId: number): Promise<VideoDetails> {
    const response = await axios.get<VideoDetails>(`/api/v1/video/${videoId}`);
    return response.data;
}

export async function getVideoReactionCounts(videoId: number): Promise<ReactionCountsResponse> {
    const response = await axios.get<ReactionCountsResponse>(`/api/v1/reactions/VIDEO/${videoId}/counts`);
    return response.data;
}

export async function getMyVideoReaction(videoId: number): Promise<ReactionCode | null> {
    const response = await axiosPrivate.get<ReactionCode | null>(`/api/v1/reactions/VIDEO/${videoId}/my`);
    return response.data;
}

export async function putVideoReaction(videoId: number, reactionCode: ReactionCode): Promise<ReactionActionResponse> {
    const response = await axiosPrivate.put<ReactionActionResponse>("/api/v1/reactions", {
        targetType: "VIDEO",
        targetId: videoId,
        reactionCode,
    });

    return response.data;
}

export async function getVideoViews(videoId: number): Promise<ViewCountsResponse> {
    const response = await axios.get<ViewCountsResponse>("/api/v1/views", {
        params: {
            targetType: "VIDEO",
            targetId: videoId,
        },
    });

    return response.data;
}

export async function recordVideoView(videoId: number): Promise<void> {
    await axios.post("/api/v1/views", null, {
        params: {
            targetType: "VIDEO",
            targetId: videoId,
        },
    });
}