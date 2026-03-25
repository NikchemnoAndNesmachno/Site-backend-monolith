import {useState} from "react";
import {useForm} from "react-hook-form";
import {zodResolver} from "@hookform/resolvers/zod";
import extractApiErrorMessage from "../../api/extractApiErrorMessage.ts";
import {replyComposerSchema, type ReplyComposerValues} from "../../validation/commentSchemas.ts";

type ReplyComposerProps = {
    isSubmitting: boolean;
    onCancel: () => void;
    onSubmitReply: (body: string) => Promise<unknown>;
};

export function ReplyComposer({ isSubmitting, onCancel, onSubmitReply }: ReplyComposerProps) {
    const [submitError, setSubmitError] = useState<string | null>(null);

    const {
        register,
        handleSubmit,
        reset,
        watch,
        formState: {errors},
    } = useForm<ReplyComposerValues>({
        resolver: zodResolver(replyComposerSchema),
        mode: "onSubmit",
        defaultValues: {
            body: "",
        },
    });

    const bodyLength = watch("body")?.length ?? 0;

    const bodyField = register("body", {
        onChange: () => {
            setSubmitError(null);
        },
    });

    const onSubmit = handleSubmit(async (values) => {
        setSubmitError(null);

        try {
            await onSubmitReply(values.body);
            reset({ body: "" });
        } catch (error) {
            setSubmitError(extractApiErrorMessage(error));
        }
    });

    function handleCancel() {
        reset({ body: "" });
        setSubmitError(null);
        onCancel();
    }

    return (
        <form className="video-page__reply-form" onSubmit={onSubmit}>
            <textarea
                className="video-page__textarea video-page__textarea--reply"
                placeholder="Write a reply..."
                maxLength={500}
                rows={3}
                disabled={isSubmitting}
                {...bodyField}
            />
            <div className="video-page__form-footer">
                <span className="video-page__form-hint">{bodyLength}/500</span>
                <div className="video-page__form-actions">
                    <button
                        type="button"
                        className="video-page__secondary-button"
                        onClick={handleCancel}
                        disabled={isSubmitting}
                    >
                        Cancel
                    </button>
                    <button
                        type="submit"
                        className="video-page__primary-button"
                        disabled={isSubmitting}
                    >
                        {isSubmitting ? "Posting..." : "Post reply"}
                    </button>
                </div>
            </div>
            {errors.body ? <p className="video-page__form-error">{errors.body.message}</p> : null}
            {submitError ? <p className="video-page__form-error">{submitError}</p> : null}
        </form>
    );
}
