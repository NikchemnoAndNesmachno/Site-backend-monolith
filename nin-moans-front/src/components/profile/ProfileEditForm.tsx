import type {PrivacySetting} from "../../types/profile.ts";
import type {FormEvent} from "react";
import type {FieldErrors, UseFormRegister} from "react-hook-form";
import type {ProfileEditFormValues} from "../../validation/profileSchemas.ts";

type PrivacyOption = {
    value: PrivacySetting;
    label: string;
    description: string;
};

type ProfileEditFormProps = {
    isEditing: boolean;
    isSubmitting: boolean;
    bioLength: number;
    errors: FieldErrors<ProfileEditFormValues>;
    register: UseFormRegister<ProfileEditFormValues>;
    privacyOptions: PrivacyOption[];
    submitError: string | null;
    onCancel: () => void;
    onSubmit: (event: FormEvent<HTMLFormElement>) => void;
};

export function ProfileEditForm({
    isEditing,
    isSubmitting,
    bioLength,
    errors,
    register,
    privacyOptions,
    submitError,
    onCancel,
    onSubmit,
}: ProfileEditFormProps) {
    return (
        <section className="profile-page__editor-card">
            <div className="profile-page__editor-header">
                <div>
                    <p className="profile-page__section-label">Profile settings</p>
                    <h2 className="profile-page__editor-title">Edit profile</h2>
                </div>
                {isEditing ? (
                    <button type="button" className="profile-page__ghost-button" onClick={onCancel} disabled={isSubmitting}>
                        Cancel
                    </button>
                ) : null}
            </div>

            {!isEditing ? (
                <div className="profile-page__read-mode">
                    <p className="profile-page__read-mode-copy">Profile is in read-only mode.</p>
                    <p className="profile-page__read-mode-hint">Use “Edit profile” above to update your details.</p>
                </div>
            ) : (
                <form className="profile-page__form" onSubmit={onSubmit}>
                    <label className="profile-page__field">
                        <span>Username</span>
                        <input
                            {...register("username")}
                            placeholder="username"
                            disabled={isSubmitting}
                        />
                        {errors.username ? <span className="profile-page__field-error">{errors.username.message}</span> : null}
                    </label>

                    <label className="profile-page__field">
                        <span>Display name</span>
                        <input
                            {...register("displayName")}
                            placeholder="Display name"
                            disabled={isSubmitting}
                        />
                        {errors.displayName ? <span className="profile-page__field-error">{errors.displayName.message}</span> : null}
                    </label>

                    <label className="profile-page__field profile-page__field--full">
                        <span>Bio</span>
                        <textarea
                            rows={5}
                            {...register("bio")}
                            placeholder="Tell something about yourself"
                            disabled={isSubmitting}
                            maxLength={280}
                        />
                        <div className="profile-page__field-meta">
                            <span className="profile-page__form-hint">{bioLength}/280</span>
                            {errors.bio ? <span className="profile-page__field-error">{errors.bio.message}</span> : null}
                        </div>
                    </label>

                    <label className="profile-page__field">
                        <span>Privacy</span>
                        <select
                            {...register("privacy")}
                            disabled={isSubmitting}
                        >
                            {privacyOptions.map((option) => (
                                <option key={option.value} value={option.value}>{option.label}</option>
                            ))}
                        </select>
                        {errors.privacy ? <span className="profile-page__field-error">{errors.privacy.message}</span> : null}
                    </label>

                    <label className="profile-page__field">
                        <span>Locale</span>
                        <input
                            {...register("locale")}
                            placeholder="en or en-US"
                            disabled={isSubmitting}
                        />
                        {errors.locale ? <span className="profile-page__field-error">{errors.locale.message}</span> : null}
                    </label>

                    <label className="profile-page__field">
                        <span>Timezone</span>
                        <input
                            {...register("timezone")}
                            placeholder="Europe/Kyiv"
                            disabled={isSubmitting}
                        />
                        {errors.timezone ? <span className="profile-page__field-error">{errors.timezone.message}</span> : null}
                    </label>

                    <div className="profile-page__form-actions">
                        <button type="submit" className="profile-page__primary-button" disabled={isSubmitting}>
                            {isSubmitting ? "Saving..." : "Save changes"}
                        </button>
                    </div>

                    {submitError ? <p className="profile-page__form-error">{submitError}</p> : null}
                </form>
            )}
        </section>
    );
}
