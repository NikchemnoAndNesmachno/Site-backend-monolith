export type UserRole = 'USER' | 'ADMIN' | string;

export type AuthContextType = {
    user: AuthUser | null;
    accessToken: string | null;
    isAuthenticated: boolean;
    isInitializing: boolean;
    login: (payload: LoginRequest) => Promise<void>;
    register: (payload: RegisterRequest) => Promise<void>;
    logout: () => Promise<void>;
    refresh: () => Promise<void>;
};

export type AuthUser = {
    id: number;
    email: string;
    role: UserRole;
};

export type AuthResponse = {
    accessToken: string;
    expiresInSeconds: number;
    // user: AuthUser;
    tokenType: string;
    userId: number;
    email: string;
    role: UserRole;
};

export type LoginRequest = {
    email: string;
    password: string;
};

export type RegisterRequest = {
    email: string;
    username: string;
    password: string;
};

export type MeResponse = {
    userId: number;
    email: string;
    status: string;
    role: UserRole;
};