import type {ReactionCode} from "../../types/video.ts";
import {formatViews} from "../../utils/formatViews.ts";
import {ReactionPanel} from "./ReactionPanel.tsx";

type VideoMetaSidebarProps = {
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
};

export function VideoMetaSidebar({
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
}: VideoMetaSidebarProps) {
    return (
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
                        <p className="video-page__author-username">@{username}</p>
                    </div>
                </div>

                <div className="video-page__stats-grid">
                    <div className="video-page__stat-card">
                        <span className="video-page__stat-label">Views</span>
                        <strong>{formatViews(totalViews)}</strong>
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
                        <strong>{new Date(createdAt).toLocaleDateString()}</strong>
                    </div>
                </div>

                <ReactionPanel
                    activeReaction={activeReaction}
                    isPending={isReactionPending}
                    canReact={canReact}
                    onReact={onReact}
                />
            </div>
        </aside>
    );
}
