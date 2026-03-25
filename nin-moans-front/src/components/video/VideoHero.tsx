import {Link} from "react-router-dom";
import {VideoPlayer} from "../VideoPlayer.tsx";
import {VideoMetaSidebar} from "./VideoMetaSidebar.tsx";
import type {ReactionCode} from "../../types/video.ts";

type VideoHeroProps = {
    sourceUrl: string;
    posterUrl: string | null;
    title: string;
    authorName: string;
    username: string;
    avatarUrl: string | null;
    totalViews: number;
    likes: number;
    dislikes: number;
    createdAt: string;
    activeReaction: ReactionCode | null;
    canReact: boolean;
    isReactionPending: boolean;
    onReact: (reactionCode: ReactionCode) => void;
    onPlaybackStart: () => void;
};

export function VideoHero({
    sourceUrl,
    posterUrl,
    title,
    authorName,
    username,
    avatarUrl,
    totalViews,
    likes,
    dislikes,
    createdAt,
    activeReaction,
    canReact,
    isReactionPending,
    onReact,
    onPlaybackStart,
}: VideoHeroProps) {
    return (
        <section className="video-page__hero-card">
            <div className="video-page__nav-row">
                <Link to="/" className="video-page__back-link">
                    ← Back to feed
                </Link>
                <span className="video-page__badge">Video details</span>
            </div>

            <div className="video-page__layout">
                <div className="video-page__player-column">
                    <VideoPlayer
                        src={sourceUrl}
                        poster={posterUrl}
                        title={title}
                        onPlaybackStart={onPlaybackStart}
                    />
                </div>

                <VideoMetaSidebar
                    authorName={authorName}
                    username={username}
                    avatarUrl={avatarUrl}
                    totalViews={totalViews}
                    likes={likes}
                    dislikes={dislikes}
                    createdAt={createdAt}
                    activeReaction={activeReaction}
                    canReact={canReact}
                    isReactionPending={isReactionPending}
                    onReact={onReact}
                />
            </div>
        </section>
    );
}
