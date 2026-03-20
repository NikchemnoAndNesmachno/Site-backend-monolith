import { axiosPrivate } from "./axios.ts";
import type { ProfileResponse, UpdateProfileRequest } from "../types/profile.ts";

export async function getMyProfile(): Promise<ProfileResponse> {
    const response = await axiosPrivate.get<ProfileResponse>("/api/v1/users/me");
    return response.data;
}

export async function updateMyProfile(payload: UpdateProfileRequest): Promise<ProfileResponse> {
    const response = await axiosPrivate.patch<ProfileResponse>("/api/v1/users/me", payload);
    return response.data;
}