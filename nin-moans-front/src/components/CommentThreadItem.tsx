import { createComment, getCommentReplies } from "../api/comments.api.ts";
import type { Comment } from "../types/comment.ts";
import {useInfiniteQuery, useMutation, useQueryClient} from "@tanstack/react-query";
import {type FormEvent, useState} from "react";
import extractApiErrorMessage from "../api/extractApiErrorMessage.ts";

function formatCommentDate(value: string | null) {
    if (!value) {
        return "Just now";
    }

    return new Intl.DateTimeFormat(undefined, {
        dateStyle: "medium",
        timeStyle: "short",
    }).format(new Date(value));
}

function getAuthorLabel(comment: Comment, currentUserId: number | null) {
    return currentUserId === comment.authorUserId ? "You" : `User #${comment.authorUserId}`;
}

type CommentThreadItemProps = {
    comment: Comment;
    videoId: number;
    currentUserId: number | null;
    canReply: boolean;
};

export function CommentThreadItem({ comment, videoId, currentUserId, canReply }: CommentThreadItemProps) {
    const queryClient = useQueryClient();
    const [isReplying, setIsReplying] = useState(false);
    const [showReplies, setShowReplies] = useState(false);
    const [replyBody, setReplyBody] = useState("");
    const [replyError, setReplyError] = useState<string | null>(null);

    const repliesQuery = useInfiniteQuery({
        queryKey: ["video", videoId, "comments", "replies", comment.id],
        queryFn: ({ pageParam }) => getCommentReplies(comment.id, pageParam, 5),
        initialPageParam: 0,
        getNextPageParam: (lastPage) => (lastPage.last ? undefined : lastPage.number + 1),
        enabled: showReplies,
        staleTime: 30_000,
    });

    const createReplyMutation = useMutation({
        mutationFn: (body: string) =>
            createComment({
                targetType: "VIDEO",
                targetId: videoId,
                parentId: comment.id,
                body,
            }),
        onSuccess: () => {
            setReplyBody("");
            setReplyError(null);
            setIsReplying(false);
            setShowReplies(true);
            void queryClient.invalidateQueries({ queryKey: ["video", videoId, "comments"] });
            void queryClient.invalidateQueries({ queryKey: ["videos"] });
        },
        onError: (error) => {
            setReplyError(extractApiErrorMessage(error));
        },
    });

    const replies = repliesQuery.data?.pages.flatMap((page) => page.content) ?? [];
    const knownReplyCount = repliesQuery.data?.pages[0]?.totalElements ?? 0;
    const replyToggleLabel = showReplies ? "Hide replies" : "Show replies";

    function handleReplySubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault();

        const trimmed = replyBody.trim();
        if (!trimmed) {
            setReplyError("Reply cannot be empty.");
            return;
        }

        setReplyError(null);
        createReplyMutation.mutate(trimmed);
    }

    return (
        <article className="video-page__comment-thread">
            <div className="video-page__comment-card">
                <header className="video-page__comment-header">
                    <div>
                        <strong className="video-page__comment-author">
                            {getAuthorLabel(comment, currentUserId)}
                        </strong>
                        <p className="video-page__comment-meta">
                            ID {comment.authorUserId} · {formatCommentDate(comment.updatedAt ?? comment.createdAt)}
                        </p>
                    </div>
                    {comment.parentId ? (
                        <span className="video-page__comment-badge">Reply</span>
                    ) : null}
                </header>

                <p className="video-page__comment-body">{comment.body}</p>

                <div className="video-page__comment-actions">
                    <button
                        type="button"
                        className="video-page__comment-action"
                        onClick={() => setShowReplies((current) => !current)}
                    >
                        {replyToggleLabel}
                        {knownReplyCount > 0 ? ` (${knownReplyCount})` : ""}
                    </button>

                    {canReply ? (
                        <button
                            type="button"
                            className="video-page__comment-action"
                            onClick={() => {
                                setIsReplying((current) => !current);
                                setShowReplies(true);
                            }}
                        >
                            Reply
                        </button>
                    ) : null}
                </div>

                {isReplying ? (
                    <form className="video-page__reply-form" onSubmit={handleReplySubmit}>
                        <textarea
                            className="video-page__textarea video-page__textarea--reply"
                            placeholder="Write a reply..."
                            value={replyBody}
                            onChange={(event) => setReplyBody(event.target.value)}
                            maxLength={500}
                            rows={3}
                        />
                        <div className="video-page__form-footer">
                            <span className="video-page__form-hint">{replyBody.length}/500</span>
                            <div className="video-page__form-actions">
                                <button
                                    type="button"
                                    className="video-page__secondary-button"
                                    onClick={() => {
                                        setIsReplying(false);
                                        setReplyBody("");
                                        setReplyError(null);
                                    }}
                                >
                                    Cancel
                                </button>
                                <button
                                    type="submit"
                                    className="video-page__primary-button"
                                    disabled={createReplyMutation.isPending}
                                >
                                    {createReplyMutation.isPending ? "Posting..." : "Post reply"}
                                </button>
                            </div>
                        </div>
                        {replyError ? <p className="video-page__form-error">{replyError}</p> : null}
                    </form>
                ) : null}
            </div>

            {showReplies ? (
                <div className="video-page__comment-children">
                    {repliesQuery.isLoading ? (
                        <p className="video-page__comment-state">Loading replies...</p>
                    ) : repliesQuery.isError ? (
                        <p className="video-page__form-error">{extractApiErrorMessage(repliesQuery.error)}</p>
                    ) : replies.length > 0 ? (
                        <>
                            {replies.map((reply) => (
                                <CommentThreadItem
                                    key={reply.id}
                                    comment={reply}
                                    videoId={videoId}
                                    currentUserId={currentUserId}
                                    canReply={canReply}
                                />
                            ))}

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
            ) : null}
        </article>
    );
}