import {useState} from "react";
import {useForm} from "react-hook-form";
import {zodResolver} from "@hookform/resolvers/zod";
import extractApiErrorMessage from "../../api/extractApiErrorMessage.ts";
import {commentComposerSchema, type CommentComposerValues} from "../../validation/commentSchemas.ts";

type CommentComposerProps = {
    isSubmitting: boolean;
    onSubmitComment: (body: string) => Promise<unknown>;
};

export function CommentComposer({ isSubmitting, onSubmitComment }: CommentComposerProps) {
    const [submitError, setSubmitError] = useState<string | null>(null);

    const {
        register,
        handleSubmit,
        reset,
        watch,
        formState: {errors},
    } = useForm<CommentComposerValues>({
        resolver: zodResolver(commentComposerSchema),
        mode: "onSubmit",
        defaultValues: {
            body: "",
        },
    });

    const commentBodyLength = watch("body")?.length ?? 0;

    const bodyField = register("body", {
        onChange: () => {
            setSubmitError(null);
        },
    });

    const onSubmit = handleSubmit(async (values) => {
        setSubmitError(null);

        try {
            await onSubmitComment(values.body);
            reset({ body: "" });
        } catch (error) {
            setSubmitError(extractApiErrorMessage(error));
        }
    });

    return (
        <form className="video-page__comment-form" onSubmit={onSubmit}>
            <textarea
                className="video-page__textarea"
                placeholder="Share what you think about this video..."
                maxLength={500}
                rows={4}
                disabled={isSubmitting}
                {...bodyField}
            />
            <div className="video-page__form-footer">
                <span className="video-page__form-hint">{commentBodyLength}/500</span>
                <button
                    type="submit"
                    className="video-page__primary-button"
                    disabled={isSubmitting}
                >
                    {isSubmitting ? "Posting..." : "Post comment"}
                </button>
            </div>
            {errors.body ? <p className="video-page__form-error">{errors.body.message}</p> : null}
            {submitError ? <p className="video-page__form-error">{submitError}</p> : null}
        </form>
    );
}
