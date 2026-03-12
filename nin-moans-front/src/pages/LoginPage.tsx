import { useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { extractApiErrorMessage, useAuth } from '../context/AuthContext';

type LoginFormState = {
    email: string;
    password: string;
};

const initialState: LoginFormState = {
    email: '',
    password: '',
};

export default function LoginPage() {
    const { login, isAuthenticated } = useAuth();
    const navigate = useNavigate();

    const [form, setForm] = useState<LoginFormState>(initialState);
    const [error, setError] = useState<string>('');
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleChange = (field: keyof LoginFormState, value: string) => {
        setForm((prev) => ({
            ...prev,
            [field]: value,
        }));
    };

    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault();
        setError('');
        setIsSubmitting(true);

        try {
            await login({
                email: form.email.trim(),
                password: form.password,
            });

            navigate('/');
        } catch (err) {
            setError(extractApiErrorMessage(err));
        } finally {
            setIsSubmitting(false);
        }
    };

    if (isAuthenticated) {
        return <div>Вы уже залогинены.</div>;
    }

    return (
        <div style={containerStyle}>
            <form onSubmit={handleSubmit} style={formStyle}>
                <h1>Login</h1>

                <label style={labelStyle}>
                    Email
                    <input
                        type="email"
                        value={form.email}
                        onChange={(e) => handleChange('email', e.target.value)}
                        required
                        maxLength={64}
                        style={inputStyle}
                    />
                </label>

                <label style={labelStyle}>
                    Password
                    <input
                        type="password"
                        value={form.password}
                        onChange={(e) => handleChange('password', e.target.value)}
                        required
                        minLength={8}
                        maxLength={72}
                        style={inputStyle}
                    />
                </label>

                {error && <div style={errorStyle}>{error}</div>}

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