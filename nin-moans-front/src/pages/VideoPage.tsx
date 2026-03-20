import {type FormEvent, useEffect, useMemo, useRef, useState} from "react";
import { Link, useParams } from "react-router-dom";
import {useInfiniteQuery, useMutation, useQuery, useQueryClient} from "@tanstack/react-query";
import useAuth from "../hooks/useAuth.ts";
import {
    getMyVideoReaction,
    getVideoDetails,
    getVideoReactionCounts,
    getVideoViews,
    putVideoReaction,
    recordVideoView,
} from "../api/video.api.ts";
import { createComment, getCommentReplies, getVideoComments } from "../api/comments.api.ts";
import type { Comment } from "../types/comment.ts";
import type { ReactionCode, ReactionCountsResponse } from "../types/video.ts";
import { formatViews } from "../utils/formatViews.ts";
import { BASE_URL } from "../api/axios.ts";
import { VideoPlayer } from "../components/VideoPlayer";
import "../styles/video.css";
import extractApiErrorMessage from "../api/extractApiErrorMessage.ts";

function parseVideoId(value: string | undefined): number | null {
    const parsed = Number(value);
    if (!Number.isInteger(parsed) || parsed <= 0) {
        return null;
    }

    return parsed;
}

function getReactionCount(counts: ReactionCountsResponse | undefined, code: ReactionCode) {
    return counts?.[code] ?? 0;
}

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

function CommentThreadItem({ comment, videoId, currentUserId, canReply }: CommentThreadItemProps) {
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

export function VideoPage() {
    const { videoId: videoIdParam } = useParams();
    const videoId = parseVideoId(videoIdParam);
    const queryClient = useQueryClient();
    const hasRecordedView = useRef(false);
    const { user } = useAuth();
    const [commentBody, setCommentBody] = useState("");
    const [commentError, setCommentError] = useState<string | null>(null);
    const [optimisticReactionState, setOptimisticReactionState] = useState<{
        videoId: number;
        reaction: ReactionCode | null;
        counts: ReactionCountsResponse;
    } | null>(null);

    const detailsQuery = useQuery({
        queryKey: ["video", videoId, "details"],
        queryFn: () => getVideoDetails(videoId!),
        enabled: videoId !== null,
    });

    const countsQuery = useQuery({
        queryKey: ["video", videoId, "counts"],
        queryFn: () => getVideoReactionCounts(videoId!),
        enabled: videoId !== null,
    });

    const viewsQuery = useQuery({
        queryKey: ["video", videoId, "views"],
        queryFn: () => getVideoViews(videoId!),
        enabled: videoId !== null,
    });

    const myReactionQuery = useQuery({
        queryKey: ["video", videoId, "my-reaction", user?.id],
        queryFn: () => getMyVideoReaction(videoId!),
        enabled: videoId !== null && Boolean(user),
    });

    const commentsQuery = useInfiniteQuery({
        queryKey: ["video", videoId, "comments", "root"],
        queryFn: ({ pageParam }) => getVideoComments(videoId!, pageParam, 10),
        initialPageParam: 0,
        getNextPageParam: (lastPage) => (lastPage.last ? undefined : lastPage.number + 1),
        enabled: videoId !== null,
        staleTime: 60_000,
    });

    const reactionMutation = useMutation({
        mutationFn: (reactionCode: ReactionCode) => putVideoReaction(videoId!, reactionCode),
        onSuccess: (response) => {
            setOptimisticReactionState({
                videoId: videoId!,
                reaction: (response.myReaction as ReactionCode | null) ?? null,
                counts: response.counts,
            });
            void queryClient.invalidateQueries({ queryKey: ["video", videoId] });
            void queryClient.invalidateQueries({ queryKey: ["videos"] });
        },
    });

    const createCommentMutation = useMutation({
        mutationFn: (body: string) =>
            createComment({
                targetType: "VIDEO",
                targetId: videoId!,
                body,
            }),
        onSuccess: () => {
            setCommentBody("");
            setCommentError(null);
            void queryClient.invalidateQueries({ queryKey: ["video", videoId, "comments"] });
            void queryClient.invalidateQueries({ queryKey: ["videos"] });
        },
        onError: (error) => {
            setCommentError(extractApiErrorMessage(error));
        },
    });

    useEffect(() => {
        hasRecordedView.current = false;
    }, [videoId]);

    useEffect(() => {
        if (!videoId || hasRecordedView.current || !detailsQuery.data) {
            return;
        }

        hasRecordedView.current = true;
        void recordVideoView(videoId)
            .then(() => queryClient.invalidateQueries({ queryKey: ["video", videoId, "views"] }))
            .catch(() => {
                hasRecordedView.current = false;
            });
    }, [detailsQuery.data, queryClient, videoId]);

    const hasOptimisticState = optimisticReactionState?.videoId === videoId;
    const activeReaction = hasOptimisticState
        ? optimisticReactionState?.reaction ?? null
        : (myReactionQuery.data ?? null);
    const counts = hasOptimisticState
        ? optimisticReactionState?.counts
        : countsQuery.data;
    const authorName = useMemo(() => {
        const author = detailsQuery.data?.author;
        if (!author) {
            return "";
        }

        return author.displayName || author.username;
    }, [detailsQuery.data?.author]);

    const rootComments = commentsQuery.data?.pages.flatMap((page) => page.content) ?? [];
    const totalRootComments = commentsQuery.data?.pages[0]?.totalElements ?? 0;

    if (videoId === null) {
        return <div className="video-page__status video-page__status--error">Invalid video id.</div>;
    }

    if (detailsQuery.isPending) {
        return <div className="video-page__status">Loading video...</div>;
    }

    if (detailsQuery.isError || !detailsQuery.data) {
        return (
            <div className="video-page__status video-page__status--error">
                Failed to load video.
            </div>
        );
    }

    const details = detailsQuery.data;
    const posterUrl = details.previewUrl ? `${BASE_URL}${details.previewUrl}` : null;
    const sourceUrl = `${BASE_URL}${details.videoUrl}`;
    const avatarUrl = details.author.avatarUrl ? `${BASE_URL}${details.author.avatarUrl}` : null;
    const likes = getReactionCount(counts, "LIKE");
    const dislikes = getReactionCount(counts, "DISLIKE");

    function handleReactionClick(reactionCode: ReactionCode) {
        reactionMutation.mutate(reactionCode);
    }

    function handleCommentSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault();

        const trimmed = commentBody.trim();
        if (!trimmed) {
            setCommentError("Comment cannot be empty.");
            return;
        }

        setCommentError(null);
        createCommentMutation.mutate(trimmed);
    }

    return (
        <main className="video-page">
            <section className="video-page__hero-card">
                <div className="video-page__nav-row">
                    <Link to="/" className="video-page__back-link">
                        ← Back to feed
                    </Link>
                    <span className="video-page__badge">Video details</span>
                </div>

                <div className="video-page__layout">
                    <div className="video-page__player-column">
                        <VideoPlayer src={sourceUrl} poster={posterUrl} title={details.title} />
                    </div>

                    <aside className="video-page__sidebar">
                        <div className="video-page__author-card">
                            <div className="video-page__author-row">
                                <div className="video-page__avatar">
                                    {avatarUrl ? (
                                        <img src={avatarUrl} alt={authorName} className="video-page__avatar-image" />
                                    ) : (
                                        <span>{authorName.charAt(0).toUpperCase()}</span>
                                    )}
                                </div>

                                <div>
                                    <p className="video-page__eyebrow">Creator</p>
                                    <h2 className="video-page__author-name">{authorName}</h2>
                                    <p className="video-page__author-username">@{details.author.username}</p>
                                </div>
                            </div>

                            <div className="video-page__stats-grid">
                                <div className="video-page__stat-card">
                                    <span className="video-page__stat-label">Views</span>
                                    <strong>{formatViews(viewsQuery.data?.totalViews ?? 0)}</strong>
                                </div>
                                <div className="video-page__stat-card">
                                    <span className="video-page__stat-label">Likes</span>
                                    <strong>{likes}</strong>
                                </div>
                                <div className="video-page__stat-card">
                                    <span className="video-page__stat-label">Dislikes</span>
                                    <strong>{dislikes}</strong>
                                </div>
                                <div className="video-page__stat-card">
                                    <span className="video-page__stat-label">Published</span>
                                    <strong>{new Date(details.createdAt).toLocaleDateString()}</strong>
                                </div>
                            </div>

                            <div className="video-page__reaction-row">
                                <button
                                    type="button"
                                    className={`video-page__reaction-button ${activeReaction === "LIKE" ? "video-page__reaction-button--active" : ""}`}
                                    onClick={() => handleReactionClick("LIKE")}
                                    disabled={reactionMutation.isPending}
                                >
                                    👍 Like
                                </button>
                                <button
                                    type="button"
                                    className={`video-page__reaction-button ${activeReaction === "DISLIKE" ? "video-page__reaction-button--active video-page__reaction-button--danger" : "video-page__reaction-button--danger"}`}
                                    onClick={() => handleReactionClick("DISLIKE")}
                                    disabled={reactionMutation.isPending}
                                >
                                    👎 Dislike
                                </button>
                            </div>
                        </div>
                    </aside>
                </div>
            </section>

            <section className="video-page__content-card">
                <p className="video-page__eyebrow">About this video</p>
                <h1 className="video-page__title">{details.title}</h1>
                <p className="video-page__description">{details.description || "Опис поки не додано."}</p>
            </section>

            <section className="video-page__content-card video-page__comments-card">
                <div className="video-page__comments-header">
                    <div>
                        <p className="video-page__eyebrow">Discussion</p>
                        <h2 className="video-page__comments-title">Threaded comments and replies</h2>
                        <p className="video-page__comments-copy">
                            Start a discussion, reply to other viewers, and keep conversations organized in threads.
                        </p>
                    </div>
                    <div className="video-page__comments-summary">
                        <strong>{totalRootComments}</strong>
                        <span>top-level comments</span>
                    </div>
                </div>

                {user ? (
                    <form className="video-page__comment-form" onSubmit={handleCommentSubmit}>
                        <textarea
                            className="video-page__textarea"
                            placeholder="Share what you think about this video..."
                            value={commentBody}
                            onChange={(event) => setCommentBody(event.target.value)}
                            maxLength={500}
                            rows={4}
                        />
                        <div className="video-page__form-footer">
                            <span className="video-page__form-hint">{commentBody.length}/500</span>
                            <button
                                type="submit"
                                className="video-page__primary-button"
                                disabled={createCommentMutation.isPending}
                            >
                                {createCommentMutation.isPending ? "Posting..." : "Post comment"}
                            </button>
                        </div>
                        {commentError ? <p className="video-page__form-error">{commentError}</p> : null}
                    </form>
                ) : (
                    <div className="video-page__login-prompt">
                        <p>Sign in to leave a comment or reply to existing threads.</p>
                        <Link to="/login" className="video-page__primary-button video-page__primary-button--link">
                            Go to login
                        </Link>
                    </div>
                )}

                <div className="video-page__comments-list">
                    {commentsQuery.isLoading ? (
                        <p className="video-page__comment-state">Loading comments...</p>
                    ) : commentsQuery.isError ? (
                        <p className="video-page__form-error">{extractApiErrorMessage(commentsQuery.error)}</p>
                    ) : rootComments.length > 0 ? (
                        <>
                            {rootComments.map((comment) => (
                                <CommentThreadItem
                                    key={comment.id}
                                    comment={comment}
                                    videoId={videoId}
                                    currentUserId={user?.id ?? null}
                                    canReply={Boolean(user)}
                                />
                            ))}

                            {commentsQuery.hasNextPage ? (
                                <button
                                    type="button"
                                    className="video-page__comment-action video-page__comment-action--load-more"
                                    onClick={() => void commentsQuery.fetchNextPage()}
                                    disabled={commentsQuery.isFetchingNextPage}
                                >
                                    {commentsQuery.isFetchingNextPage ? "Loading more..." : "Load more comments"}
                                </button>
                            ) : null}
                        </>
                    ) : (
                        <p className="video-page__comment-state">No comments yet. Be the first to start the conversation.</p>
                    )}
                </div>
            </section>
        </main>
    );
}
