import {useForm} from "react-hook-form";
import {zodResolver} from "@hookform/resolvers/zod";
import {useLocation} from "react-router-dom";
import {useLoginMutation} from "../hooks/useLoginMutation.ts";
import {type LoginFormValues, loginSchema} from "../validation/authSchemas.ts";
import extractApiErrorMessage from "../api/extractApiErrorMessage.ts";
import {AuthPageLayout} from "../components/auth/AuthPageLayout.tsx";
import {AuthCard} from "../components/auth/AuthCard.tsx";
import {AuthFieldError} from "../components/auth/AuthFieldError.tsx";

export default function LoginPage() {
    const loginMutation = useLoginMutation();
    const location = useLocation();

    const {
        register,
        handleSubmit,
        formState: {errors, isSubmitting},
    } = useForm<LoginFormValues>({
        resolver: zodResolver(loginSchema),
        defaultValues: {
            email: "",
            password: "",
        },
        mode: "onSubmit",
    });

    const onSubmit = async (data: LoginFormValues) => {
        await loginMutation.mutateAsync(data);
    };

    const serverError = loginMutation.error
        ? extractApiErrorMessage(loginMutation.error)
        : "";

    const successMessage =
        location.state && (location.state as {registered?: boolean}).registered
            ? "Registration completed. Please sign in."
            : "";

    return (
        <AuthPageLayout>
            <AuthCard title="Sign in">
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
                                    if (loginMutation.isError) {
                                        loginMutation.reset();
                                    }
                                },
                            })}
                        />
                        <AuthFieldError message={errors.email?.message} />
                    </label>

                    <label className="auth-form__label">
                        Password
                        <input
                            type="password"
                            maxLength={72}
                            autoComplete="current-password"
                            className="auth-form__input"
                            {...register("password", {
                                onChange: () => {
                                    if (loginMutation.isError) {
                                        loginMutation.reset();
                                    }
                                },
                            })}
                        />
                        <AuthFieldError message={errors.password?.message} />
                    </label>

                    <AuthFieldError message={serverError} />
                    {successMessage ? <p className="auth-form__success">{successMessage}</p> : null}

                    <button type="submit" disabled={isSubmitting} className="auth-form__button">
                        {isSubmitting ? "Signing in..." : "Sign in"}
                    </button>
                </form>
            </AuthCard>
        </AuthPageLayout>
    );
}
