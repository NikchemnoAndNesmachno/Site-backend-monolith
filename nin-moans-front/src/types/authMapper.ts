import type {AuthResponse, AuthUser} from "./auth.ts";

export default function mapAuth(data: AuthResponse): AuthUser {
    return {
        id: data.userId,
        email: data.email,
        role: data.role,
    };
}