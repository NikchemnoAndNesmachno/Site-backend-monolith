import type {ReactNode} from "react";

type AuthCardProps = {
    title: string;
    children: ReactNode;
};

export function AuthCard({ title, children }: AuthCardProps) {
    return (
        <section className="auth-card" aria-label={title}>
            <h1 className="auth-card__title">{title}</h1>
            {children}
        </section>
    );
}
