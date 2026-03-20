export type PrivacySetting = "PUBLIC" | "FRIENDS_ONLY" | "PRIVATE";

export type ProfileResponse = {
    userId: number;
    username: string;
    displayName: string;
    bio: string | null;
    privacy: PrivacySetting;
    locale: string | null;
    timezone: string | null;
    createdAt: string | null;
    updatedAt: string | null;
};

export type UpdateProfileRequest = {
    username: string;
    displayName: string;
    bio: string;
    privacy: PrivacySetting;
    locale: string;
    timezone: string;
};