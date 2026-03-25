import {useEffect, useMemo, useState} from "react";
import {useMutation, useQuery, useQueryClient} from "@tanstack/react-query";
import {zodResolver} from "@hookform/resolvers/zod";
import {useForm} from "react-hook-form";
import useAuth from "../hooks/useAuth.ts";
import {getMyProfile, updateMyProfile} from "../api/profile.api.ts";
import type {PrivacySetting, ProfileResponse, UpdateProfileRequest} from "../types/profile.ts";
import {profileEditSchema, type ProfileEditFormValues} from "../validation/profileSchemas.ts";
import {ProfileHeroCard} from "../components/profile/ProfileHeroCard.tsx";
import {ProfileSummaryCard} from "../components/profile/ProfileSummaryCard.tsx";
import {ProfileEditForm} from "../components/profile/ProfileEditForm.tsx";
import extractApiErrorMessage from "../api/extractApiErrorMessage.ts";
import "../styles/profile.css";

const PRIVACY_OPTIONS: Array<{ value: PrivacySetting; label: string; description: string }> = [
    { value: "PUBLIC", label: "Public", description: "Your profile is visible to everyone" },
    { value: "FRIENDS_ONLY", label: "Friends only", description: "Only your friends can view the profile" },
    { value: "PRIVATE", label: "Private", description: "Your profile is hidden from everyone else" },
];

function mapProfileToForm(profile: ProfileResponse): ProfileEditFormValues {
    return {
        username: profile.username ?? "",
        displayName: profile.displayName ?? "",
        bio: profile.bio ?? "",
        privacy: profile.privacy,
        locale: profile.locale ?? "",
        timezone: profile.timezone ?? "",
    };
}

function mapFormToUpdatePayload(values: ProfileEditFormValues): UpdateProfileRequest {
    return {
        username: values.username,
        displayName: values.displayName,
        bio: values.bio,
        privacy: values.privacy,
        locale: values.locale,
        timezone: values.timezone,
    };
}

export function ProfilePage() {
    const queryClient = useQueryClient();
    const {user, logout} = useAuth();
    const [isEditing, setIsEditing] = useState(false);

    const profileQuery = useQuery({
        queryKey: ["profile", "me"],
        queryFn: getMyProfile,
    });

    const {
        register,
        handleSubmit,
        reset,
        watch,
        formState: {errors},
    } = useForm<ProfileEditFormValues>({
        resolver: zodResolver(profileEditSchema),
        mode: "onSubmit",
        defaultValues: {
            username: "",
            displayName: "",
            bio: "",
            privacy: "PUBLIC",
            locale: "",
            timezone: "",
        },
    });

    const updateMutation = useMutation({
        mutationFn: updateMyProfile,
        onSuccess: (nextProfile) => {
            queryClient.setQueryData(["profile", "me"], nextProfile);
            reset(mapProfileToForm(nextProfile));
            setIsEditing(false);
        },
    });

    const profile = profileQuery.data;

    useEffect(() => {
        if (!profile) {
            return;
        }

        reset(mapProfileToForm(profile));
    }, [profile, reset]);

    const bioLength = watch("bio")?.length ?? 0;

    const submitError = useMemo(() => {
        if (!updateMutation.isError) {
            return null;
        }

        return extractApiErrorMessage(updateMutation.error);
    }, [updateMutation.error, updateMutation.isError]);

    if (profileQuery.isPending) {
        return <div className="profile-page__status">Loading profile...</div>;
    }

    if (profileQuery.isError || !profile) {
        return <div className="profile-page__status profile-page__status--error">Failed to load profile.</div>;
    }

    const resolvedProfile = profile;

    function handleEnterEditMode() {
        reset(mapProfileToForm(resolvedProfile));
        setIsEditing(true);
    }

    function handleCancelEditMode() {
        reset(mapProfileToForm(resolvedProfile));
        setIsEditing(false);
    }

    function onSubmit(values: ProfileEditFormValues) {
        const payload = mapFormToUpdatePayload(values);
        updateMutation.mutate(payload);
    }

    return (
        <main className="profile-page">
            <ProfileHeroCard
                profile={resolvedProfile}
                isEditing={isEditing}
                onEdit={handleEnterEditMode}
                onLogout={() => void logout()}
            />

            <section className="profile-page__layout">
                <ProfileSummaryCard profile={resolvedProfile} user={user} privacyOptions={PRIVACY_OPTIONS} />

                <ProfileEditForm
                    isEditing={isEditing}
                    isSubmitting={updateMutation.isPending}
                    bioLength={bioLength}
                    errors={errors}
                    register={register}
                    privacyOptions={PRIVACY_OPTIONS}
                    submitError={submitError}
                    onCancel={handleCancelEditMode}
                    onSubmit={handleSubmit(onSubmit)}
                />
            </section>
        </main>
    );
}
