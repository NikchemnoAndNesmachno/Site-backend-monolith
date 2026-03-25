import {z} from "zod";

export const USERNAME_PATTERN = /^[a-zA-Z0-9._]+$/;

export const usernameSchema = z.string()
    .trim()
    .min(3, "Username must be at least 3 characters.")
    .max(64, "Username must be 64 characters or fewer.")
    .refine((value) => USERNAME_PATTERN.test(value), "Use letters, numbers, dot, or underscore.");

