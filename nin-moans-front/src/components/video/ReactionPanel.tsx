import {Link} from "react-router-dom";
import type {ReactionCode} from "../../types/video.ts";

type ReactionPanelProps = {
    activeReaction: ReactionCode | null;
    isPending: boolean;
    canReact: boolean;
    onReact: (reactionCode: ReactionCode) => void;
};

export function ReactionPanel({ activeReaction, isPending, canReact, onReact }: ReactionPanelProps) {
    return (
        <div className="video-page__reaction-row">
            <button
                type="button"
                className={`video-page__reaction-button ${activeReaction === "LIKE" ? "video-page__reaction-button--active" : ""}`}
                onClick={() => onReact("LIKE")}
                disabled={isPending || !canReact}
            >
                👍 Like
            </button>
            <button
                type="button"
                className={`video-page__reaction-button ${activeReaction === "DISLIKE" ? "video-page__reaction-button--active video-page__reaction-button--danger" : "video-page__reaction-button--danger"}`}
                onClick={() => onReact("DISLIKE")}
                disabled={isPending || !canReact}
            >
                👎 Dislike
            </button>

            {!canReact ? (
                <p className="video-page__reaction-hint">
                    <Link to="/login">Sign in</Link> to react.
                </p>
            ) : null}
        </div>
    );
}
