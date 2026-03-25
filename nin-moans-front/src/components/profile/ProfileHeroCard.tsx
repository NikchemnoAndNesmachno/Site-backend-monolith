import {Link} from "react-router-dom";
import type {ProfileResponse} from "../../types/profile.ts";

type ProfileHeroCardProps = {
    profile: ProfileResponse;
    isEditing: boolean;
    onEdit: () => void;
    onLogout: () => void;
};

export function ProfileHeroCard({ profile, isEditing, onEdit, onLogout }: ProfileHeroCardProps) {
    return (
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
                    <p className="profile-page__bio">{profile.bio || "Add a bio to make your profile more personal."}</p>
                </div>

                <div className="profile-page__hero-actions">
                    <button
                        type="button"
                        className="profile-page__secondary-button"
                        onClick={onEdit}
                        disabled={isEditing}
                    >
                        Edit profile
                    </button>
                    <button
                        type="button"
                        className="profile-page__ghost-button"
                        onClick={onLogout}
                    >
                        Logout
                    </button>
                </div>
            </div>
        </section>
    );
}
