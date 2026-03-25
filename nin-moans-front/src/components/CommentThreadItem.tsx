import {createComment} from "../api/comments.api.ts";
import type {Comment} from "../types/comment.ts";
import {useMutation, useQueryClient} from "@tanstack/react-query";
import {useState} from "react";
import {CommentCard} from "./comments/CommentCard.tsx";
import {ReplyComposer} from "./comments/ReplyComposer.tsx";
import {CommentReplies} from "./comments/CommentReplies.tsx";

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
    const [knownReplyCount, setKnownReplyCount] = useState(0);

    const createReplyMutation = useMutation({
        mutationFn: (body: string) =>
            createComment({
                targetType: "VIDEO",
                targetId: videoId,
                parentId: comment.id,
                body,
            }),
        onSuccess: () => {
            setIsReplying(false);
            setShowReplies(true);
            void queryClient.invalidateQueries({ queryKey: ["video", videoId, "comments"] });
            void queryClient.invalidateQueries({ queryKey: ["video", videoId, "comments", "replies", comment.id] });
        },
    });

    function handleToggleReplyComposer() {
        setIsReplying((current) => !current);
        setShowReplies(true);
    }

    return (
        <article className="video-page__comment-thread">
            <CommentCard
                comment={comment}
                currentUserId={currentUserId}
                canReply={canReply}
                showReplies={showReplies}
                knownReplyCount={knownReplyCount}
                onToggleReplies={() => setShowReplies((current) => !current)}
                onToggleReplyComposer={handleToggleReplyComposer}
            />

            {isReplying ? (
                <ReplyComposer
                    isSubmitting={createReplyMutation.isPending}
                    onCancel={() => setIsReplying(false)}
                    onSubmitReply={(body) => createReplyMutation.mutateAsync(body)}
                />
            ) : null}

            <CommentReplies
                videoId={videoId}
                parentCommentId={comment.id}
                showReplies={showReplies}
                onReplyCountChange={setKnownReplyCount}
                renderReply={(reply) => (
                    <CommentThreadItem
                        key={reply.id}
                        comment={reply}
                        videoId={videoId}
                        currentUserId={currentUserId}
                        canReply={canReply}
                    />
                )}
            />
        </article>
    );
}
