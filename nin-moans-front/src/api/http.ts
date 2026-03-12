import axios, { AxiosError, type InternalAxiosRequestConfig } from 'axios';
import type { AuthResponse } from '../types/auth';

const API_BASE_URL = 'http://localhost:8080'; // backend base url

export const http = axios.create({
    baseURL: API_BASE_URL,
    withCredentials: true, // критично для HttpOnly refresh cookie
    headers: {
        'Content-Type': 'application/json',
    },
});

let accessToken: string | null = null;
let refreshPromise: Promise<string | null> | null = null;

export const tokenStorage = {
    getAccessToken: () => accessToken,
    setAccessToken: (token: string | null) => {
        accessToken = token;
    },
};

type AuthHandlers = {
    onAuthSuccess: (data: AuthResponse) => void;
    onLogout: () => void;
};

let authHandlers: AuthHandlers | null = null;

export const bindAuthHandlers = (handlers: AuthHandlers) => {
    authHandlers = handlers;
};

http.interceptors.request.use((config: InternalAxiosRequestConfig) => {
    const token = tokenStorage.getAccessToken();

    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
});

http.interceptors.response.use(
    (response) => response,
    async (error: AxiosError) => {
        const originalRequest = error.config as InternalAxiosRequestConfig & {
            _retry?: boolean;
        };

        const status = error.response?.status;
        const url = originalRequest?.url ?? '';

        const isAuthEndpoint =
            url.includes('/api/v1/auth/login') ||
            url.includes('/api/v1/auth/register') ||
            url.includes('/api/v1/auth/refresh') ||
            url.includes('/api/v1/auth/logout');

        if (status !== 401 || !originalRequest || originalRequest._retry || isAuthEndpoint) {
            return Promise.reject(error);
        }

        originalRequest._retry = true;

        try {
            if (!refreshPromise) {
                refreshPromise = http
                    .post<AuthResponse>('/api/v1/auth/refresh')
                    .then((res) => {
                        tokenStorage.setAccessToken(res.data.accessToken);
                        authHandlers?.onAuthSuccess(res.data);
                        return res.data.accessToken;
                    })
                    .catch((refreshError) => {
                        tokenStorage.setAccessToken(null);
                        authHandlers?.onLogout();
                        throw refreshError;
                    })
                    .finally(() => {
                        refreshPromise = null;
                    });
            }

            const newAccessToken = await refreshPromise;

            if (newAccessToken) {
                originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
            }

            return http(originalRequest);
        } catch (refreshError) {
            return Promise.reject(refreshError);
        }
    }
);