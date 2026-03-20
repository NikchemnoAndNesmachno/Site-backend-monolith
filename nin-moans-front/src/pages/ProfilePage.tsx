import { useMemo, useState, type FormEvent } from "react";
import { Link } from "react-router-dom";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import useAuth from "../hooks/useAuth.ts";
import { getMyProfile, updateMyProfile } from "../api/profile.api.ts";
import type { PrivacySetting, ProfileResponse, UpdateProfileRequest } from "../types/profile.ts";
import "../styles/profile.css";

const PRIVACY_OPTIONS: Array<{ value: PrivacySetting; label: string; description: string }> = [
    { value: "PUBLIC", label: "Public", description: "Профіль бачать всі" },
    { value: "FRIENDS_ONLY", label: "Friends only", description: "Профіль можуть побачити лише люди зі списку друзів" },
    { value: "PRIVATE", label: "Private", description: "Профіль прихований від інших" },
];

function createDraft(profile: ProfileResponse): UpdateProfileRequest {
    return {
        username: profile.username ?? "",
        displayName: profile.displayName ?? "",
        bio: profile.bio ?? "",
        privacy: profile.privacy,
        locale: profile.locale ?? "",
        timezone: profile.timezone ?? "",
    };
}

function formatDate(value: string | null) {
    if (!value) {
        return "—";
    }

    return new Date(value).toLocaleString();
}

export function ProfilePage() {
    const queryClient = useQueryClient();
    const { user, logout } = useAuth();
    const [draft, setDraft] = useState<UpdateProfileRequest | null>(null);

    const profileQuery = useQuery({
        queryKey: ["profile", "me"],
        queryFn: getMyProfile,
    });

    const updateMutation = useMutation({
        mutationFn: updateMyProfile,
        onSuccess: (profile) => {
            queryClient.setQueryData(["profile", "me"], profile);
            setDraft(null);
        },
    });

    const profile = profileQuery.data;
    const activeDraft = draft ?? (profile ? createDraft(profile) : null);
    const selectedPrivacy = useMemo(
        () => PRIVACY_OPTIONS.find((option) => option.value === (activeDraft?.privacy ?? "PUBLIC")),
        [activeDraft?.privacy],
    );

    if (profileQuery.isPending) {
        return <div className="profile-page__status">Loading profile...</div>;
    }

    if (profileQuery.isError || !profile) {
        return <div className="profile-page__status profile-page__status--error">Failed to load profile.</div>;
    }

    function handleFieldChange<K extends keyof UpdateProfileRequest>(field: K, value: UpdateProfileRequest[K]) {
        setDraft({
            ...createDraft(profile!),
            ...draft,
            [field]: value,
        });
    }

    function handleCancel() {
        setDraft(null);
    }

    function handleSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault();
        if (!activeDraft) {
            return;
        }

        updateMutation.mutate(activeDraft);
    }

    return (
        <main className="profile-page">
            <section className="profile-page__hero-card">
                <div className="profile-page__nav-row">
                    <Link to="/" className="profile-page__back-link">← Back to feed</Link>
                    <span className="profile-page__badge">Profile</span>
                </div>

                <div className="profile-page__hero-content">
                    <div>
                        <p className="profile-page__eyebrow">Account overview</p>
                        <h1 className="profile-page__title">{profile.displayName || profile.username}</h1>
                        <p className="profile-page__subtitle">@{profile.username}</p>
                        <p className="profile-page__bio">{profile.bio || "Додайте bio, щоб профіль виглядав більш живим."}</p>
                    </div>

                    <div className="profile-page__hero-actions">
                        <button
                            type="button"
                            className="profile-page__secondary-button"
                            onClick={() => setDraft(createDraft(profile))}
                        >
                            Edit profile
                        </button>
                        <button
                            type="button"
                            className="profile-page__ghost-button"
                            onClick={() => void logout()}
                        >
                            Logout
                        </button>
                    </div>
                </div>
            </section>

            <section className="profile-page__layout">
                <aside className="profile-page__summary-card">
                    <div className="profile-page__summary-block">
                        <span className="profile-page__section-label">Identity</span>
                        <strong>{user?.email}</strong>
                        <span>Role: {user?.role}</span>
                        <span>User ID: {profile.userId}</span>
                    </div>

                    <div className="profile-page__summary-block">
                        <span className="profile-page__section-label">Visibility</span>
                        <strong>{selectedPrivacy?.label}</strong>
                        <span>{selectedPrivacy?.description}</span>
                    </div>

                    <div className="profile-page__summary-grid">
                        <div className="profile-page__summary-item">
                            <span>Locale</span>
                            <strong>{profile.locale || "—"}</strong>
                        </div>
                        <div className="profile-page__summary-item">
                            <span>Timezone</span>
                            <strong>{profile.timezone || "—"}</strong>
                        </div>
                        <div className="profile-page__summary-item">
                            <span>Created</span>
                            <strong>{formatDate(profile.createdAt)}</strong>
                        </div>
                        <div className="profile-page__summary-item">
                            <span>Updated</span>
                            <strong>{formatDate(profile.updatedAt)}</strong>
                        </div>
                    </div>
                </aside>

                <section className="profile-page__editor-card">
                    <div className="profile-page__editor-header">
                        <div>
                            <p className="profile-page__section-label">Profile settings</p>
                            <h2 className="profile-page__editor-title">Редагування профілю</h2>
                        </div>
                        {draft ? (
                            <button type="button" className="profile-page__ghost-button" onClick={handleCancel}>
                                Cancel
                            </button>
                        ) : null}
                    </div>

                    <form className="profile-page__form" onSubmit={handleSubmit}>
                        <label className="profile-page__field">
                            <span>Username</span>
                            <input
                                value={activeDraft?.username ?? ""}
                                onChange={(event) => handleFieldChange("username", event.target.value)}
                                placeholder="username"
                            />
                        </label>

                        <label className="profile-page__field">
                            <span>Display name</span>
                            <input
                                value={activeDraft?.displayName ?? ""}
                                onChange={(event) => handleFieldChange("displayName", event.target.value)}
                                placeholder="Display name"
                            />
                        </label>

                        <label className="profile-page__field profile-page__field--full">
                            <span>Bio</span>
                            <textarea
                                rows={5}
                                value={activeDraft?.bio ?? ""}
                                onChange={(event) => handleFieldChange("bio", event.target.value)}
                                placeholder="Розкажіть що-небудь про себе"
                            />
                        </label>

                        <label className="profile-page__field">
                            <span>Privacy</span>
                            <select
                                value={activeDraft?.privacy ?? "PUBLIC"}
                                onChange={(event) => handleFieldChange("privacy", event.target.value as PrivacySetting)}
                            >
                                {PRIVACY_OPTIONS.map((option) => (
                                    <option key={option.value} value={option.value}>{option.label}</option>
                                ))}
                            </select>
                        </label>

                        <label className="profile-page__field">
                            <span>Locale</span>
                            <input
                                value={activeDraft?.locale ?? ""}
                                onChange={(event) => handleFieldChange("locale", event.target.value)}
                                placeholder="en / uk"
                            />
                        </label>

                        <label className="profile-page__field">
                            <span>Timezone</span>
                            <input
                                value={activeDraft?.timezone ?? ""}
                                onChange={(event) => handleFieldChange("timezone", event.target.value)}
                                placeholder="Europe/Kyiv"
                            />
                        </label>

                        <div className="profile-page__form-actions">
                            <button type="submit" className="profile-page__primary-button" disabled={updateMutation.isPending}>
                                {updateMutation.isPending ? "Saving..." : "Save changes"}
                            </button>
                        </div>

                        {updateMutation.isError ? (
                            <p className="profile-page__form-error">Failed to save profile changes.</p>
                        ) : null}
                    </form>
                </section>
            </section>
        </main>
    );
}