import { useMutation } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import useAuth from '../hooks/useAuth';
import type { RegisterRequest } from '../types/auth';

export function useRegisterMutation() {
    const { register } = useAuth();
    const navigate = useNavigate();

    return useMutation({
        mutationFn: async (payload: RegisterRequest) => {
            await register(payload);
        },
        onSuccess: () => {
            navigate('/login', {
                replace: true,
                state: { registered: true },
            });
        },
    });
}