import { AxiosError, type InternalAxiosRequestConfig } from 'axios';
import {axiosPrivate} from "./axios.ts";
import {performRefresh} from "./refresh.ts";
import {tokenStorage} from "./tokenStorage.ts";

axiosPrivate.interceptors.request.use((config: InternalAxiosRequestConfig) => {
    const token = tokenStorage.getAccessToken();

    if (!config.headers.Authorization && token) {
        config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
});

axiosPrivate.interceptors.response.use(
    (response) => response,
    async (error: AxiosError) => {
        const originalRequest = error.config as (InternalAxiosRequestConfig & { sent: boolean })

        if (!originalRequest) {
            return Promise.reject(error);
        }

        const url = originalRequest.url ?? '';

        const isAuthEndpoint =
            url.includes('/api/v1/auth/login') ||
            url.includes('/api/v1/auth/register') ||
            url.includes('/api/v1/auth/refresh') ||
            url.includes('/api/v1/auth/logout');

        if (error.response?.status !== 401 || originalRequest.sent || isAuthEndpoint) {
            return Promise.reject(error);
        }

        originalRequest.sent = true;

        try {
            const refreshData = await performRefresh();
            const newAccessToken = refreshData.accessToken;

            if (!newAccessToken) {
                return Promise.reject(error);
            }

            originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
            return axiosPrivate(originalRequest);
        } catch (refreshError) {
            return Promise.reject(refreshError);
        }
    }
);