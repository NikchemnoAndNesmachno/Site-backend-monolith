import { useEffect, useMemo, useRef, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import useAuth from "../hooks/useAuth.ts";
import {
    getMyVideoReaction,
    getVideoDetails,
    getVideoReactionCounts,
    getVideoViews,
    putVideoReaction,
    recordVideoView,
} from "../api/video.api.ts";
import type { ReactionCode, ReactionCountsResponse } from "../types/video.ts";
import { formatViews } from "../utils/formatViews.ts";
import { BASE_URL } from "../api/axios.ts";
import { VideoPlayer } from "../components/VideoPlayer";
import "../styles/video.css";

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

export function VideoPage() {
    const { videoId: videoIdParam } = useParams();
    const videoId = parseVideoId(videoIdParam);
    const queryClient = useQueryClient();
    const hasRecordedView = useRef(false);
    const { user } = useAuth();
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
                <p className="video-page__description">{details.description || "Описание пока не добавлено."}</p>
            </section>
        </main>
    );
}
