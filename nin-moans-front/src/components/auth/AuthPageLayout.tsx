import type {ReactNode} from "react";
import "../../styles/auth.css";

type AuthPageLayoutProps = {
    children: ReactNode;
};

export function AuthPageLayout({ children }: AuthPageLayoutProps) {
    return (
        <main className="auth-page">
            <div className="auth-page__container">{children}</div>
        </main>
    );
}
