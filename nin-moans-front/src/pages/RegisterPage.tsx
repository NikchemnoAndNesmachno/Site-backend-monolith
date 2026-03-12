import { useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { extractApiErrorMessage, useAuth } from '../context/AuthContext';

type RegisterFormState = {
    email: string;
    username: string;
    password: string;
    confirmPassword: string;
};

const initialState: RegisterFormState = {
    email: '',
    username: '',
    password: '',
    confirmPassword: '',
};

export default function RegisterPage() {
    const { register } = useAuth();
    const navigate = useNavigate();

    const [form, setForm] = useState<RegisterFormState>(initialState);
    const [error, setError] = useState<string>('');
    const [successMessage, setSuccessMessage] = useState<string>('');
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleChange = (field: keyof RegisterFormState, value: string) => {
        setForm((prev) => ({
            ...prev,
            [field]: value,
        }));
    };

    const validate = (): string | null => {
        if (form.password !== form.confirmPassword) {
            return 'Passwords do not match';
        }

        if (form.username.trim().length < 3) {
            return 'Username must be at least 3 characters';
        }

        return null;
    };

    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault();
        setError('');
        setSuccessMessage('');

        const validationError = validate();
        if (validationError) {
            setError(validationError);
            return;
        }

        setIsSubmitting(true);

        try {
            await register({
                email: form.email.trim(),
                username: form.username.trim(),
                password: form.password,
            });

            setSuccessMessage('Registration completed. Now log in.');
            navigate('/login');
        } catch (err) {
            setError(extractApiErrorMessage(err));
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div style={containerStyle}>
            <form onSubmit={handleSubmit} style={formStyle}>
                <h1>Register</h1>

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
                    Username
                    <input
                        type="text"
                        value={form.username}
                        onChange={(e) => handleChange('username', e.target.value)}
                        required
                        minLength={3}
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

                <label style={labelStyle}>
                    Confirm password
                    <input
                        type="password"
                        value={form.confirmPassword}
                        onChange={(e) => handleChange('confirmPassword', e.target.value)}
                        required
                        minLength={8}
                        maxLength={72}
                        style={inputStyle}
                    />
                </label>

                {error && <div style={errorStyle}>{error}</div>}
                {successMessage && <div style={successStyle}>{successMessage}</div>}

                <button type="submit" disabled={isSubmitting} style={buttonStyle}>
                    {isSubmitting ? 'Registering...' : 'Register'}
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