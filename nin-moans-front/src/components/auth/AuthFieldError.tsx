type AuthFieldErrorProps = {
    message?: string;
};

export function AuthFieldError({ message }: AuthFieldErrorProps) {
    if (!message) {
        return null;
    }

    return <p className="auth-form__error">{message}</p>;
}
