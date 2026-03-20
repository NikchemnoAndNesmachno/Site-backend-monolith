import api, { axiosPrivate } from "./axios.ts";
import type { Comment, CreateCommentRequest, SpringPage } from "../types/comment.ts";

export async function getVideoComments(
    videoId: number,
    page = 0,
    size = 10,
): Promise<SpringPage<Comment>> {
    const response = await api.get<SpringPage<Comment>>("/api/v1/comments", {
        params: {
            targetType: "VIDEO",
            targetId: videoId,
            page,
            size,
        },
    });

    return response.data;
}

export async function getCommentReplies(
    parentId: number,
    page = 0,
    size = 5,
): Promise<SpringPage<Comment>> {
    const response = await api.get<SpringPage<Comment>>(`/api/v1/comments/${parentId}/replies`, {
        params: {
            page,
            size,
        },
    });

    return response.data;
}

export async function createComment(payload: CreateCommentRequest): Promise<Comment> {
    const response = await axiosPrivate.post<Comment>("/api/v1/comments", payload);
    return response.data;
}