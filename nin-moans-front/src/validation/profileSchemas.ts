import {z} from "zod";
import {usernameSchema} from "./sharedSchemas.ts";

const LOCALE_REGEX = /^[a-z]{2,3}(?:-[A-Z]{2})?$/;
const IANA_TIMEZONE_REGEX = /^[A-Za-z_]+\/[A-Za-z0-9_+\-]+(?:\/[A-Za-z0-9_+\-]+)?$/;

export const profileEditSchema = z.object({
    username: z.string().pipe(usernameSchema),
    displayName: z.string()
        .min(1, "Display name is required.")
        .max(80, "Display name must be 80 characters or fewer.")
        .transform((value) => value.trim()),
    bio: z.string()
        .max(280, "Bio must be 280 characters or fewer.")
        .transform((value) => value.trim()),
    privacy: z.enum(["PUBLIC", "FRIENDS_ONLY", "PRIVATE"]),
    locale: z.string()
        .max(16, "Locale must be 16 characters or fewer.")
        .transform((value) => value.trim())
        .refine((value) => value.length === 0 || LOCALE_REGEX.test(value), {
            message: "Use locale format like en, uk, or en-US.",
        }),
    timezone: z.string()
        .max(64, "Timezone must be 64 characters or fewer.")
        .transform((value) => value.trim())
        .refine((value) => value.length === 0 || IANA_TIMEZONE_REGEX.test(value), {
            message: "Use an IANA timezone like Europe/Kyiv.",
        }),
});

export type ProfileEditFormValues = z.infer<typeof profileEditSchema>;
