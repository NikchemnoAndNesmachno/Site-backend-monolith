let accessToken: string | null = null;

export const tokenStorage = {
    getAccessToken: () => accessToken,
    setAccessToken: (token: string | null) => {
        accessToken = token;
    },
};