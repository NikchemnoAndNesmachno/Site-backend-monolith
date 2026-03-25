import {useEffect, useMemo, useRef} from "react";
import {Link, useParams} from "react-router-dom";
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
import {createComment, getVideoComments} from "../api/comments.api.ts";
import type {ReactionCode, ReactionCountsResponse} from "../types/video.ts";
import {BASE_URL} from "../api/axios.ts";
import "../styles/video.css";
import extractApiErrorMessage from "../api/extractApiErrorMessage.ts";
import {CommentComposer} from "../components/comments/CommentComposer.tsx";
import {CommentThreadList} from "../components/comments/CommentThreadList.tsx";
import {VideoHero} from "../components/video/VideoHero.tsx";

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

function buildOptimisticCounts(
    previousCounts: ReactionCountsResponse | undefined,
    previousReaction: ReactionCode | null,
    nextReaction: ReactionCode,
): ReactionCountsResponse {
    const nextCounts: ReactionCountsResponse = {
        LIKE: previousCounts?.LIKE ?? 0,
        DISLIKE: previousCounts?.DISLIKE ?? 0,
    };

    if (previousReaction && previousReaction !== nextReaction) {
        nextCounts[previousReaction] = Math.max((nextCounts[previousReaction] ?? 0) - 1, 0);
    }

    if (previousReaction !== nextReaction) {
        nextCounts[nextReaction] = (nextCounts[nextReaction] ?? 0) + 1;
    }

    return nextCounts;
}

export function VideoPage() {
    const {videoId: videoIdParam} = useParams();
    const videoId = parseVideoId(videoIdParam);
    const queryClient = useQueryClient();
    const hasRecordedView = useRef(false);
    const {user} = useAuth();
    const canReact = Boolean(user);
    const backTo = user ? "/" : "/login";

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
        queryFn: ({pageParam}) => getVideoComments(videoId!, pageParam, 10),
        initialPageParam: 0,
        getNextPageParam: (lastPage) => (lastPage.last ? undefined : lastPage.number + 1),
        enabled: videoId !== null,
        staleTime: 60_000,
    });

    const reactionMutation = useMutation({
        mutationFn: (reactionCode: ReactionCode) => putVideoReaction(videoId!, reactionCode),
        onMutate: async (reactionCode) => {
            const countsKey = ["video", videoId, "counts"] as const;
            const myReactionKey = ["video", videoId, "my-reaction", user?.id] as const;

            await queryClient.cancelQueries({queryKey: countsKey});
            await queryClient.cancelQueries({queryKey: myReactionKey});

            const previousCounts = queryClient.getQueryData<ReactionCountsResponse>(countsKey);
            const previousReaction = queryClient.getQueryData<ReactionCode | null>(myReactionKey) ?? null;

            queryClient.setQueryData<ReactionCountsResponse>(
                countsKey,
                buildOptimisticCounts(previousCounts, previousReaction, reactionCode),
            );
            queryClient.setQueryData<ReactionCode | null>(myReactionKey, reactionCode);

            return {
                countsKey,
                myReactionKey,
                previousCounts,
                previousReaction,
            };
        },
        onError: (_error, _reactionCode, context) => {
            if (!context) {
                return;
            }

            queryClient.setQueryData(context.countsKey, context.previousCounts);
            queryClient.setQueryData(context.myReactionKey, context.previousReaction);
        },
        onSuccess: (response) => {
            queryClient.setQueryData(["video", videoId, "counts"], response.counts);
            queryClient.setQueryData(["video", videoId, "my-reaction", user?.id], response.myReaction ?? null);
        },
        onSettled: () => {
            void queryClient.invalidateQueries({queryKey: ["video", videoId, "counts"]});
            void queryClient.invalidateQueries({queryKey: ["video", videoId, "my-reaction"]});
            void queryClient.invalidateQueries({queryKey: ["videos"]});
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
            void queryClient.invalidateQueries({queryKey: ["video", videoId, "comments"]});
            void queryClient.invalidateQueries({queryKey: ["videos"]});
        },
    });

    const recordViewMutation = useMutation({
        mutationFn: (targetVideoId: number) => recordVideoView(targetVideoId),
        onSuccess: () => {
            void queryClient.invalidateQueries({queryKey: ["video", videoId, "views"]});
        },
        onError: () => {
            hasRecordedView.current = false;
        },
    });

    useEffect(() => {
        hasRecordedView.current = false;
    }, [videoId]);

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
    const activeReaction = myReactionQuery.data ?? null;
    const likes = getReactionCount(countsQuery.data, "LIKE");
    const dislikes = getReactionCount(countsQuery.data, "DISLIKE");

    function handleReactionClick(reactionCode: ReactionCode) {
        if (!canReact) {
            return;
        }

        reactionMutation.mutate(reactionCode);
    }

    function handlePlaybackStart() {
        if (!videoId || hasRecordedView.current || recordViewMutation.isPending) {
            return;
        }

        hasRecordedView.current = true;
        recordViewMutation.mutate(videoId);
    }

    return (
        <main className="video-page">
            <VideoHero
                backTo={backTo}
                sourceUrl={sourceUrl}
                posterUrl={posterUrl}
                title={details.title}
                authorName={authorName}
                username={details.author.username}
                avatarUrl={avatarUrl}
                totalViews={viewsQuery.data?.totalViews ?? 0}
                likes={likes}
                dislikes={dislikes}
                createdAt={details.createdAt}
                activeReaction={activeReaction}
                canReact={canReact}
                isReactionPending={reactionMutation.isPending}
                onReact={handleReactionClick}
                onPlaybackStart={handlePlaybackStart}
            />

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
                    <CommentComposer
                        isSubmitting={createCommentMutation.isPending}
                        onSubmitComment={(body) => createCommentMutation.mutateAsync(body)}
                    />
                ) : (
                    <div className="video-page__login-prompt">
                        <p>Sign in to leave a comment or reply to existing threads.</p>
                        <Link to="/login" className="video-page__primary-button video-page__primary-button--link">
                            Go to login
                        </Link>
                    </div>
                )}

                <div className="video-page__comments-list">
                    <CommentThreadList
                        comments={rootComments}
                        isLoading={commentsQuery.isLoading}
                        isError={commentsQuery.isError}
                        errorMessage={commentsQuery.isError ? extractApiErrorMessage(commentsQuery.error) : null}
                        hasNextPage={Boolean(commentsQuery.hasNextPage)}
                        isFetchingNextPage={commentsQuery.isFetchingNextPage}
                        onLoadMore={() => void commentsQuery.fetchNextPage()}
                        videoId={videoId}
                        currentUserId={user?.id ?? null}
                        canReply={Boolean(user)}
                    />
                </div>
            </section>
        </main>
    );
}
