import {useRegisterMutation} from "../hooks/useRegisterMutation.ts";
import {type RegisterFormValues, registerSchema} from "../validation/authSchemas.ts";
import {useForm} from "react-hook-form";
import {zodResolver} from "@hookform/resolvers/zod";
import extractApiErrorMessage from "../api/extractApiErrorMessage.ts";
import useAuth from "../hooks/useAuth.ts";
import {Navigate} from "react-router-dom";
import {AuthPageLayout} from "../components/auth/AuthPageLayout.tsx";
import {AuthCard} from "../components/auth/AuthCard.tsx";
import {AuthFieldError} from "../components/auth/AuthFieldError.tsx";

export default function RegisterPage() {
    const {isAuthenticated} = useAuth();
    const registerMutation = useRegisterMutation();

    const {
        register,
        handleSubmit,
        formState: {errors, isSubmitting},
    } = useForm<RegisterFormValues>({
        resolver: zodResolver(registerSchema),
        defaultValues: {
            email: "",
            username: "",
            password: "",
            confirmPassword: "",
        },
        mode: "onSubmit",
    });

    const onSubmit = async (data: RegisterFormValues) => {
        await registerMutation.mutateAsync(data);
    };

    const serverError = registerMutation.error
        ? extractApiErrorMessage(registerMutation.error)
        : "";

    if (isAuthenticated) {
        return <Navigate to="/" replace />;
    }

    return (
        <AuthPageLayout>
            <AuthCard title="Create account">
                <form onSubmit={handleSubmit(onSubmit)} className="auth-form" noValidate>
                    <label className="auth-form__label">
                        Email
                        <input
                            type="email"
                            maxLength={64}
                            autoComplete="email"
                            className="auth-form__input"
                            {...register("email", {
                                onChange: () => {
                                    if (registerMutation.isError) {
                                        registerMutation.reset();
                                    }
                                },
                            })}
                        />
                        <AuthFieldError message={errors.email?.message} />
                    </label>

                    <label className="auth-form__label">
                        Username
                        <input
                            type="text"
                            maxLength={64}
                            autoComplete="username"
                            className="auth-form__input"
                            {...register("username", {
                                onChange: () => {
                                    if (registerMutation.isError) {
                                        registerMutation.reset();
                                    }
                                },
                            })}
                        />
                        <AuthFieldError message={errors.username?.message} />
                    </label>

                    <label className="auth-form__label">
                        Password
                        <input
                            type="password"
                            maxLength={72}
                            autoComplete="new-password"
                            className="auth-form__input"
                            {...register("password", {
                                onChange: () => {
                                    if (registerMutation.isError) {
                                        registerMutation.reset();
                                    }
                                },
                            })}
                        />
                        <AuthFieldError message={errors.password?.message} />
                    </label>

                    <label className="auth-form__label">
                        Confirm password
                        <input
                            type="password"
                            maxLength={72}
                            autoComplete="new-password"
                            className="auth-form__input"
                            {...register("confirmPassword", {
                                onChange: () => {
                                    if (registerMutation.isError) {
                                        registerMutation.reset();
                                    }
                                },
                            })}
                        />
                        <AuthFieldError message={errors.confirmPassword?.message} />
                    </label>

                    <AuthFieldError message={serverError} />

                    <button type="submit" disabled={isSubmitting} className="auth-form__button">
                        {isSubmitting ? "Creating account..." : "Create account"}
                    </button>
                </form>
            </AuthCard>
        </AuthPageLayout>
    );
}
