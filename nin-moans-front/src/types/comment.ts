export type CommentStatus = "ACTIVE" | "DELETED";

export type Comment = {
    id: number;
    authorUserId: number;
    targetType: string;
    targetId: number;
    parentId: number | null;
    body: string;
    status: CommentStatus;
    createdAt: string | null;
    updatedAt: string | null;
};

export type CreateCommentRequest = {
    targetType: string;
    targetId: number;
    parentId?: number | null;
    body: string;
};

export type SpringPage<T> = {
    content: T[];
    totalPages: number;
    totalElements: number;
    size: number;
    number: number;
    numberOfElements: number;
    first: boolean;
    last: boolean;
    empty: boolean;
};