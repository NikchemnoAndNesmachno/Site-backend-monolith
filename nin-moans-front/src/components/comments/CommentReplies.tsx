import {useEffect, type ReactNode} from "react";
import {useInfiniteQuery} from "@tanstack/react-query";
import {getCommentReplies} from "../../api/comments.api.ts";
import extractApiErrorMessage from "../../api/extractApiErrorMessage.ts";
import type {Comment} from "../../types/comment.ts";

type CommentRepliesProps = {
    videoId: number;
    parentCommentId: number;
    showReplies: boolean;
    onReplyCountChange: (count: number | null) => void;
    renderReply: (reply: Comment) => ReactNode;
};

export function CommentReplies({
    videoId,
    parentCommentId,
    showReplies,
    onReplyCountChange,
    renderReply,
}: CommentRepliesProps) {
    const repliesQuery = useInfiniteQuery({
        queryKey: ["video", videoId, "comments", "replies", parentCommentId],
        queryFn: ({ pageParam }) => getCommentReplies(parentCommentId, pageParam, 5),
        initialPageParam: 0,
        getNextPageParam: (lastPage) => (lastPage.last ? undefined : lastPage.number + 1),
        enabled: showReplies,
        staleTime: 30_000,
    });

    useEffect(() => {
        if (!repliesQuery.data) {
            return;
        }

        const total = repliesQuery.data.pages[0]?.totalElements ?? 0;
        onReplyCountChange(total);
    }, [onReplyCountChange, repliesQuery.data]);

    if (!showReplies) {
        return null;
    }

    const replies = repliesQuery.data?.pages.flatMap((page) => page.content) ?? [];

    return (
        <div className="video-page__comment-children">
            {repliesQuery.isLoading ? (
                <p className="video-page__comment-state">Loading replies...</p>
            ) : repliesQuery.isError ? (
                <p className="video-page__form-error">{extractApiErrorMessage(repliesQuery.error)}</p>
            ) : replies.length > 0 ? (
                <>
                    {replies.map((reply) => renderReply(reply))}

                    {repliesQuery.hasNextPage ? (
                        <button
                            type="button"
                            className="video-page__comment-action video-page__comment-action--load-more"
                            onClick={() => void repliesQuery.fetchNextPage()}
                            disabled={repliesQuery.isFetchingNextPage}
                        >
                            {repliesQuery.isFetchingNextPage ? "Loading more..." : "Load more replies"}
                        </button>
                    ) : null}
                </>
            ) : (
                <p className="video-page__comment-state">No replies yet.</p>
            )}
        </div>
    );
}
