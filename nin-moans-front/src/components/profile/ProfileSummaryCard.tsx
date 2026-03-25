import type {AuthUser} from "../../types/auth.ts";
import type {ProfileResponse, PrivacySetting} from "../../types/profile.ts";

type PrivacyOption = {
    value: PrivacySetting;
    label: string;
    description: string;
};

type ProfileSummaryCardProps = {
    profile: ProfileResponse;
    user: AuthUser | null;
    privacyOptions: PrivacyOption[];
};

function formatDate(value: string | null) {
    if (!value) {
        return "—";
    }

    return new Date(value).toLocaleString();
}

export function ProfileSummaryCard({ profile, user, privacyOptions }: ProfileSummaryCardProps) {
    const selectedPrivacy = privacyOptions.find((option) => option.value === profile.privacy);

    return (
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
    );
}
