package ua.nin.common.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StringEndpoints {
    public static final String API_V1_AUTH_REGISTER = "/api/v1/auth/register";
    public static final String API_V1_AUTH_LOGIN = "/api/v1/auth/login";
    public static final String API_V1_AUTH_LOGOUT = "/api/v1/auth/logout";
    public static final String API_V1_AUTH_LOGOUT_ALL = "/api/v1/auth/logout-all";
    public static final String API_V1_AUTH_REFRESH = "/api/v1/auth/refresh";
    public static final String API_V1_AUTH_ME = "/api/v1/auth/me";

    public static final String API_V1_AUTH_EMAIL_VERIFY = "/api/v1/auth/email/verify";
    public static final String API_V1_AUTH_EMAIL_RESEND = "/api/v1/auth/email/resend";

    public static final String API_V1_AUTH_PASSWORD_FORGOT = "/api/v1/auth/password/forgot";
    public static final String API_V1_AUTH_PASSWORD_RESET = "/api/v1/auth/password/reset";
    public static final String API_V1_AUTH_PASSWORD_CHANGE = "/api/v1/auth/password/change";

    public static final String API_V1_USERS_ME = "/api/v1/users/me";
    public static final String API_V1_USERS_BY_USERNAME = "/api/v1/users/by-username/{username}";

    public static final String API_V1_MEDIA_UPLOAD = "/api/v1/media/upload";
    public static final String API_V1_MEDIA_BY_ID = "/api/v1/media/{mediaId}";
    public static final String API_V1_MEDIA_BY_ID_META = "/api/v1/media/{mediaId}/meta";

    public static final String ACTUATOR = "/actuator/**";
    public static final String V3_API_DOCS = "/v3/api-docs/**";
    public static final String SWAGGER_UI = "/swagger-ui/**";
    public static final String SWAGGER_UI_HTML = "/swagger-ui.html";

    public static final String API_V1_AVATAR_UPLOAD = "/api/v1/avatar/upload";
    public static final String API_V1_AVATAR_BY_ID = "/api/v1/avatar/{avatarId}";

    public static final String API_V1_VIDEO_UPLOAD_WITH_PREVIEW = "/api/v1/video/upload/video-with-preview";
    public static final String API_V1_VIDEO_BY_ID = "/api/v1/video/{videoId}";

    public static final String API_V1_REACTIONS = "/api/v1/reactions";
    public static final String API_V1_REACTIONS_BY_TARGET_TYPE_BY_TARGET_ID_COUNTS = "/api/v1/reactions/{targetType}/{targetId}/counts";
    public static final String API_V1_REACTIONS_BY_TARGET_TYPE_BY_TARGET_ID_MY = "/api/v1/reactions/{targetType}/{targetId}/my";

    public static final String API_V1_COMMENTS = "/api/v1/comments";
    public static final String API_V1_COMMENTS_BY_PARENT_ID_REPLIES = "/api/v1/comments/{parentId}/replies";
    public static final String API_V1_COMMENTS_BY_ID = "/api/v1/comments/{commentId}";

    public static final String API_V1_VIEWS = "/api/v1/views";

    public static final String API_V1_FEED = "/api/v1/feed";
}
