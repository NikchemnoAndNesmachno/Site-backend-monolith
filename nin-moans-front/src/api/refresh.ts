import type { AuthResponse } from '../types/auth';
import {axiosRefresh} from "./axios.ts";
import {tokenStorage} from "./tokenStorage.ts";

let refreshPromise: Promise<AuthResponse> | null = null;

type AuthHandlers = {
    onAuthSuccess: (data: AuthResponse) => void;
    onLogout: () => void;
};

let authHandlers: AuthHandlers | null = null;

export const bindAuthHandlers = (handlers: AuthHandlers) => {
    authHandlers = handlers;
};

export const performRefresh = async (): Promise<AuthResponse> => {
    if (!refreshPromise) {
        refreshPromise = axiosRefresh
            .post<AuthResponse>('/api/v1/auth/refresh')
            .then((res) => {
                tokenStorage.setAccessToken(res.data.accessToken);
                return res.data;
            })
            .catch((error) => {
                tokenStorage.setAccessToken(null);
                authHandlers?.onLogout();
                throw error;
            })
            .finally(() => {
                refreshPromise = null;
            });
    }

    return refreshPromise;
};