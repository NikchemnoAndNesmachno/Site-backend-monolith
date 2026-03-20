export type FeedSort = "LATEST" | "POPULAR";
export type ReactionCode = "LIKE" | "DISLIKE";
export type ReactionCountsResponse = Partial<Record<ReactionCode, number>>;

export type ViewCountsResponse = {
    totalViews: number;
    uniqueViews: number;
};

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
    myReaction: ReactionCode | null;
    createdAt: string;
    // durationSeconds: number;
    author: VideoAuthor;
};

export type VideoDetails = {
    videoId: number;
    title: string;
    description: string;
    videoMediaId: number;
    videoUrl: string;
    previewMediaId: number;
    previewUrl: string;
    author: VideoAuthor;
    createdAt: string;
};

export type ReactionActionResponse = {
    targetType: string;
    targetId: number;
    myReaction: ReactionCode | null;
    counts: ReactionCountsResponse;
    updatedAt: string;
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