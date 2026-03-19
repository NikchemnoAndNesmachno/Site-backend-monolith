import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import useAuth from "../hooks/useAuth.ts";
import {useLoginMutation} from "../hooks/useLoginMutation.ts";
import {type LoginFormValues, loginSchema} from "../validation/authSchemas.ts";
import {useLocation} from "react-router-dom";
import extractApiErrorMessage from "../api/extractApiErrorMessage.ts";

export default function LoginPage() {
    const { isAuthenticated } = useAuth();
    const loginMutation = useLoginMutation();
    const location = useLocation();

    const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<LoginFormValues>({
        resolver: zodResolver(loginSchema),
        defaultValues: {
            email: '',
            password: '',
        },
        mode: 'onSubmit',
    });

    const onSubmit = async (data: LoginFormValues) => {
        await loginMutation.mutateAsync(data);
    };

    const serverError = loginMutation.error
        ? extractApiErrorMessage(loginMutation.error)
        : '';

    const successMessage =
        location.state && (location.state as { registered?: boolean }).registered
            ? 'Registration completed. Now log in.'
            : '';

    if (isAuthenticated) {
        return <div>Ви вже увійшли.</div>;
    }

    return (
        <div style={containerStyle}>
            <form onSubmit={handleSubmit(onSubmit)} style={formStyle} noValidate>
                <h1>Login</h1>

                <label style={labelStyle}>
                    Email
                    <input
                        type="email"
                        maxLength={64}
                        style={inputStyle}
                        {...register('email')}
                    />
                    {errors.email && <div style={errorStyle}>{errors.email.message}</div>}
                </label>

                <label style={labelStyle}>
                    Password
                    <input
                        type="password"
                        maxLength={72}
                        style={inputStyle}
                        {...register('password')}
                    />
                    {errors.password && <div style={errorStyle}>{errors.password.message}</div>}
                </label>

                {serverError && <div style={errorStyle}>{serverError}</div>}
                {successMessage && <div style={successStyle}>{successMessage}</div>}

                <button type="submit" disabled={isSubmitting} style={buttonStyle}>
                    {isSubmitting ? 'Logging in...' : 'Login'}
                </button>
            </form>
        </div>
    );
}

const containerStyle: React.CSSProperties = {
    minHeight: '100vh',
    display: 'grid',
    placeItems: 'center',
    padding: '24px',
};

const formStyle: React.CSSProperties = {
    width: '100%',
    maxWidth: '420px',
    display: 'flex',
    flexDirection: 'column',
    gap: '16px',
    padding: '24px',
    border: '1px solid #ccc',
    borderRadius: '12px',
};

const labelStyle: React.CSSProperties = {
    display: 'flex',
    flexDirection: 'column',
    gap: '8px',
};

const inputStyle: React.CSSProperties = {
    padding: '10px 12px',
    fontSize: '16px',
};

const buttonStyle: React.CSSProperties = {
    padding: '12px',
    fontSize: '16px',
    cursor: 'pointer',
};

const errorStyle: React.CSSProperties = {
    color: 'crimson',
    fontSize: '14px',
};

const successStyle: React.CSSProperties = {
    color: 'green',
    fontSize: '14px',
};