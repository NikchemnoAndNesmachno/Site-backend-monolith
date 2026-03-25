import {z} from "zod";

const commentBodySchema = z.string()
    .trim()
    .min(1, "Comment cannot be empty.")
    .max(500, "Comment must be 500 characters or fewer.");

export const commentComposerSchema = z.object({
    body: commentBodySchema,
});

export const replyComposerSchema = z.object({
    body: commentBodySchema,
});

export type CommentComposerValues = z.infer<typeof commentComposerSchema>;
export type ReplyComposerValues = z.infer<typeof replyComposerSchema>;
