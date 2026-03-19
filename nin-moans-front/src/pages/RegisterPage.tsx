import {useRegisterMutation} from "../hooks/useRegisterMutation.ts";
import {type RegisterFormValues, registerSchema} from "../validation/authSchemas.ts";
import {useForm} from "react-hook-form";
import {zodResolver} from "@hookform/resolvers/zod";
import extractApiErrorMessage from "../api/extractApiErrorMessage.ts";

export default function RegisterPage() {
    const registerMutation = useRegisterMutation();

    const {register, handleSubmit, formState: { errors, isSubmitting } } = useForm<RegisterFormValues>({
        resolver: zodResolver(registerSchema),
        defaultValues: {
            email: '',
            username: '',
            password: '',
            confirmPassword: '',
        },
        mode: 'onSubmit',
    });

    const onSubmit = async (data: RegisterFormValues) => {
        await registerMutation.mutateAsync(data);
    };

    const serverError = registerMutation.error
        ? extractApiErrorMessage(registerMutation.error)
        : '';

    return (
        <div style={containerStyle}>
            <form onSubmit={handleSubmit(onSubmit)} style={formStyle} noValidate>
                <h1>Register</h1>

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
                    Username
                    <input
                        type="text"
                        maxLength={64}
                        style={inputStyle}
                        {...register('username')}
                    />
                    {errors.username && <div style={errorStyle}>{errors.username.message}</div>}
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

                <label style={labelStyle}>
                    Confirm password
                    <input
                        type="password"
                        maxLength={72}
                        style={inputStyle}
                        {...register('confirmPassword')}
                    />
                    {errors.confirmPassword && <div style={errorStyle}>{errors.confirmPassword.message}</div>}
                </label>

                {serverError && <div style={errorStyle}>{serverError}</div>}

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