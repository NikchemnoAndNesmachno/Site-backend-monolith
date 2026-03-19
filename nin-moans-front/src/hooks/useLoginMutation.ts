import { useMutation } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import useAuth from '../hooks/useAuth';
import type {LoginRequest} from "../types/auth.ts";

export function useLoginMutation() {
    const { login } = useAuth();
    const navigate = useNavigate();

    return useMutation({
        mutationFn: async (payload: LoginRequest) => {
            await login(payload);
        },
        onSuccess: () => {
            navigate('/');
        },
    });
}