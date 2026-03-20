export type FeedSort = "LATEST" | "POPULAR";

export type VideoAuthor = {
    userId: number;
    username: string;
    displayName: string;
    avatarMediaId: number;
    avatarUrl: string;
};

export type VideoListItem = {
    videoId: number;
    title: string;
    description: string;
    previewMediaId: number;
    previewUrl: string;
    viewsCount: number;
    likesCount: number;
    dislikesCount: number;
    commentsCount: number;
    myReaction: boolean;
    createdAt: string;
    // durationSeconds: number;
    author: VideoAuthor;
};

export type PageResponse<T> = {
    items: T[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
    hasNext: boolean;
    hasPrevious: boolean;
};