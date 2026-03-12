import {
    createContext,
    useCallback,
    useContext,
    useEffect,
    useMemo,
    useState,
    type ReactNode,
} from 'react';
import { AxiosError } from 'axios';
import { bindAuthHandlers, http, tokenStorage } from '../api/http';
import type {
    AuthResponse,
    AuthUser,
    LoginRequest,
    RegisterRequest,
} from '../types/auth';

type AuthContextType = {
    user: AuthUser | null;
    accessToken: string | null;
    isAuthenticated: boolean;
    isInitializing: boolean;
    login: (payload: LoginRequest) => Promise<void>;
    register: (payload: RegisterRequest) => Promise<void>;
    logout: () => Promise<void>;
    refresh: () => Promise<void>;
};

const AuthContext = createContext<AuthContextType | null>(null);

function mapAuth(data: AuthResponse): AuthUser {

    return {
        id: data.userId,
        role: data.role
    };
}

export const AuthProvider = ({ children }: { children: ReactNode }) => {
    const [user, setUser] = useState<AuthUser | null>(null);
    const [accessToken, setAccessToken] = useState<string | null>(null);
    const [isInitializing, setIsInitializing] = useState(true);

    const applyAuthData = (data: AuthResponse) => {
        tokenStorage.setAccessToken(data.accessToken);
        setAccessToken(data.accessToken);
        setUser(mapAuth(data));
    };

    const clearAuth = useCallback(() => {
        tokenStorage.setAccessToken(null);
        setAccessToken(null);
        setUser(null);
    }, []);

    const refresh = useCallback(async () => {
        const response = await http.post<AuthResponse>('/api/v1/auth/refresh');
        applyAuthData(response.data);
    }, []);

    const login = useCallback(async (payload: LoginRequest) => {
        const response = await http.post<AuthResponse>('/api/v1/auth/login', payload);
        applyAuthData(response.data);
    }, []);

    const register = useCallback(async (payload: RegisterRequest) => {
        await http.post('/api/v1/auth/register', payload);
    }, []);

    const logout = useCallback(async () => {
        try {
            await http.post('/api/v1/auth/logout');
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
    }, [clearAuth]);

    useEffect(() => {
        const bootstrapAuth = async () => {
            try {
                await refresh();
            } catch (error) {
                clearAuth();
            } finally {
                setIsInitializing(false);
            }
        };

        void bootstrapAuth();
    }, [refresh, clearAuth]);

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

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = (): AuthContextType => {
    const context = useContext(AuthContext);

    if (!context) {
        throw new Error('useAuth must be used inside AuthProvider');
    }

    return context;
};

export const extractApiErrorMessage = (error: unknown): string => {
    if (error instanceof AxiosError) {
        const data = error.response?.data;

        if (typeof data === 'string') {
            return data;
        }

        if (data && typeof data === 'object') {
            if ('message' in data && typeof data.message === 'string') {
                return data.message;
            }

            if ('error' in data && typeof data.error === 'string') {
                return data.error;
            }
        }

        return error.message || 'Request failed';
    }

    if (error instanceof Error) {
        return error.message;
    }

    return 'Unknown error';
};