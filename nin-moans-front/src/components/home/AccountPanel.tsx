import {Link} from "react-router-dom";
import type {AuthUser} from "../../types/auth.ts";

type AccountPanelProps = {
    user: AuthUser;
    onLogout: () => void;
};

export function AccountPanel({ user, onLogout }: AccountPanelProps) {
    return (
        <aside className="home-page__profile-card">
            <div className="home-page__profile-header">
                <span className="home-page__profile-badge">Акаунт</span>
                <div className="home-page__profile-actions">
                    <Link to="/profile" className="home-page__profile-link">Profile</Link>
                    <button
                        type="button"
                        className="home-page__logout-button"
                        onClick={onLogout}
                    >
                        Logout
                    </button>
                </div>
            </div>

            <div className="home-page__profile-details">
                <strong>{user.email}</strong>
                <span>ID: {user.id}</span>
                <span>Role: {user.role}</span>
            </div>
        </aside>
    );
}
