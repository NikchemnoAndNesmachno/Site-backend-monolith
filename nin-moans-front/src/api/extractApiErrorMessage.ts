import {AxiosError} from "axios";

export default (error: unknown): string => {
    if (error instanceof AxiosError) {
        const data = error.response?.data;

        if (typeof data === 'string') {
            return data;
        }

        if (data && typeof data === 'object') {
            if ('message' in data && typeof data.message === 'string') {
                return data.message;
            }

            if ('error' in data && typeof data.error === 'string') {
                return data.error;
            }
        }

        return error.message || 'Request failed';
    }

    if (error instanceof Error) {
        return error.message;
    }

    return 'Unknown error';
};