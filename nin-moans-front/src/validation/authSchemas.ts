import { z } from 'zod';
import {usernameSchema} from "./sharedSchemas.ts";

export const loginSchema = z.object({
    email: z
        .email('Invalid email')
        .trim()
        .min(1, 'Email is required'),
    password: z
        .string()
        .min(8, 'Password must be at least 8 characters')
        .max(72, 'Password must be at most 72 characters'),
});

export const registerSchema = z
    .object({
        email: z
            .email('Invalid email')
            .trim()
            .min(1, 'Email is required'),
        username: z
            .string()
            .pipe(usernameSchema),
        password: z
            .string()
            .min(8, 'Password must be at least 8 characters')
            .max(72, 'Password must be at most 72 characters'),
        confirmPassword: z
            .string()
            .min(8, 'Confirm password must be at least 8 characters')
            .max(72, 'Confirm password must be at most 72 characters'),
    })
    .refine((data) => data.password === data.confirmPassword, {
        message: 'Passwords do not match',
        path: ['confirmPassword'],
    });

export type LoginFormValues = z.infer<typeof loginSchema>;
export type RegisterFormValues = z.infer<typeof registerSchema>;
