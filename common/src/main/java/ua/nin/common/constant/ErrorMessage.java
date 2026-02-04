package ua.nin.common.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ErrorMessage {
    public static final String MEDIA_NOT_FOUND = "Media not found";
    public static final String VIDEO_NOT_FOUND = "Video not found";
    public static final String AVATAR_NOT_FOUND = "User avatar not found";
    public static final String USER_NOT_FOUND = "User not found";
    public static final String FILE_NOT_FOUND = "File not found on storage";
    public static final String INVALID_CREDENTIALS  = "Invalid credentials";
    public static final String FORBIDDEN_LOGIN = "User is not allowed to login";
    public static final String EMAIL_ALREADY_EXISTS = "Email already exists";
    public static final String USERNAME_ALREADY_EXISTS = "Username already exists";
    public static final String CANNOT_STORE_MEDIA = "Cannot store media";
    public static final String CANNOT_FINALIZE_MEDIA = "Cannot finalize media";
    public static final String USER_NOT_ALLOWED_TO_DELETE_AVATAR = "You are not allowed to delete this avatar image";
    public static final String USER_NOT_ALLOWED_TO_DELETE_VIDEO = "You are not allowed to delete this video";
}
