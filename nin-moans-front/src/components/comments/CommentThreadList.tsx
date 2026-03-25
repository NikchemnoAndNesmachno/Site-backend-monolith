import {CommentThreadItem} from "../CommentThreadItem.tsx";
import type {Comment} from "../../types/comment.ts";

type CommentThreadListProps = {
    comments: Comment[];
    isLoading: boolean;
    isError: boolean;
    errorMessage: string | null;
    hasNextPage: boolean;
    isFetchingNextPage: boolean;
    onLoadMore: () => void;
    videoId: number;
    currentUserId: number | null;
    canReply: boolean;
};

export function CommentThreadList({
    comments,
    isLoading,
    isError,
    errorMessage,
    hasNextPage,
    isFetchingNextPage,
    onLoadMore,
    videoId,
    currentUserId,
    canReply,
}: CommentThreadListProps) {
    if (isLoading) {
        return <p className="video-page__comment-state">Loading comments...</p>;
    }

    if (isError) {
        return <p className="video-page__form-error">{errorMessage ?? "Failed to load comments."}</p>;
    }

    if (comments.length === 0) {
        return <p className="video-page__comment-state">No comments yet. Be the first to start the conversation.</p>;
    }

    return (
        <>
            {comments.map((comment) => (
                <CommentThreadItem
                    key={comment.id}
                    comment={comment}
                    videoId={videoId}
                    currentUserId={currentUserId}
                    canReply={canReply}
                />
            ))}

            {hasNextPage ? (
                <button
                    type="button"
                    className="video-page__comment-action video-page__comment-action--load-more"
                    onClick={onLoadMore}
                    disabled={isFetchingNextPage}
                >
                    {isFetchingNextPage ? "Loading more..." : "Load more comments"}
                </button>
            ) : null}
        </>
    );
}
