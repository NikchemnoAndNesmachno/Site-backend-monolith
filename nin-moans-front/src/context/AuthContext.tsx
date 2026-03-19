import {
    createContext,
    useCallback,
    useEffect,
    useMemo,
    useState,
    type ReactNode,
} from 'react';
import type {
    AuthContextType,
    AuthResponse,
    AuthUser,
    LoginRequest,
    RegisterRequest,
} from '../types/auth';
import {axiosPrivate} from "../api/axios.ts";
import {tokenStorage} from "../api/tokenStorage.ts";
import {bindAuthHandlers, performRefresh} from "../api/refresh.ts";
import mapAuth from "../types/authMapper.ts";

export const AuthContext = createContext<AuthContextType | null>(null);

/**
 * Module-level bootstrap dedupe.
 * Це переживає StrictMode remount в dev.
 */
let bootstrapAuthPromise: Promise<void> | null = null;

export const AuthProvider = ({ children }: { children: ReactNode }) => {
    const [user, setUser] = useState<AuthUser | null>(null);
    const [accessToken, setAccessToken] = useState<string | null>(null);
    const [isInitializing, setIsInitializing] = useState(true);

    const applyAuthData = useCallback((data: AuthResponse) => {
        tokenStorage.setAccessToken(data.accessToken);
        setAccessToken(data.accessToken);
        setUser(mapAuth(data));
    }, []);

    const clearAuth = useCallback(() => {
        tokenStorage.setAccessToken(null);
        setAccessToken(null);
        setUser(null);
    }, []);

    const refresh = useCallback(async () => {
        const data = await performRefresh();
        applyAuthData(data);
    }, [applyAuthData]);

    const login = useCallback(
        async (payload: LoginRequest) => {
            const response = await axiosPrivate.post<AuthResponse>('/api/v1/auth/login', payload);
            applyAuthData(response.data);
        },
        [applyAuthData]
    );

    const register = useCallback(async (payload: RegisterRequest) => {
        await axiosPrivate.post('/api/v1/auth/register', payload);
    }, []);

    const logout = useCallback(async () => {
        try {
            await axiosPrivate.post('/api/v1/auth/logout');
        } finally {
            clearAuth();
        }
    }, [clearAuth]);

    useEffect(() => {
        bindAuthHandlers({
            onAuthSuccess: (data) => {
                applyAuthData(data);
            },
            onLogout: () => {
                clearAuth();
            },
        });
    }, [applyAuthData, clearAuth]);

    useEffect(() => {
        if (!bootstrapAuthPromise) {
            bootstrapAuthPromise = (async () => {
                try {
                    const data = await performRefresh();
                    applyAuthData(data);
                } catch {
                    clearAuth();
                }
            })().finally(() => {
                bootstrapAuthPromise = null;
            });
        }

        void bootstrapAuthPromise.finally(() => {
            setIsInitializing(false);
        });
    }, [applyAuthData, clearAuth]);

    const value = useMemo<AuthContextType>(
        () => ({
            user,
            accessToken,
            isAuthenticated: !!user && !!accessToken,
            isInitializing,
            login,
            register,
            logout,
            refresh,
        }),
        [user, accessToken, isInitializing, login, register, logout, refresh]
    );

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};