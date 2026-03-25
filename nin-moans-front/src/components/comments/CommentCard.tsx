import type {Comment} from "../../types/comment.ts";

type CommentCardProps = {
    comment: Comment;
    currentUserId: number | null;
    canReply: boolean;
    showReplies: boolean;
    knownReplyCount: number | null;
    onToggleReplies: () => void;
    onToggleReplyComposer: () => void;
};

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

export function CommentCard({
    comment,
    currentUserId,
    canReply,
    showReplies,
    knownReplyCount,
    onToggleReplies,
    onToggleReplyComposer,
}: CommentCardProps) {
    const replyToggleLabel = showReplies ? "Hide replies" : "Show replies";

    return (
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
                    onClick={onToggleReplies}
                >
                    {replyToggleLabel}
                    {knownReplyCount !== null ? ` (${knownReplyCount})` : ""}
                </button>

                {canReply ? (
                    <button
                        type="button"
                        className="video-page__comment-action"
                        onClick={onToggleReplyComposer}
                    >
                        Reply
                    </button>
                ) : null}
            </div>
        </div>
    );
}
