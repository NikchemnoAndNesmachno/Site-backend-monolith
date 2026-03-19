import { useSearchParams } from "react-router-dom";
import { useVideosPage } from "../hooks/useVideosPage";
import { VideoGrid } from "../components/VideoGrid";
import { VideoPagination } from "../components/VideoPagination";
import useAuth from "../hooks/useAuth.ts";

const DEFAULT_PAGE = 0;
const DEFAULT_SIZE = 20;

function parsePositiveInt(value: string | null, fallback: number): number {
    if (!value) return fallback;

    const parsed = Number(value);
    if (!Number.isInteger(parsed) || parsed < 0) {
        return fallback;
    }

    return parsed;
}

export function HomePage() {
    const [searchParams, setSearchParams] = useSearchParams();

    const page = parsePositiveInt(searchParams.get("page"), DEFAULT_PAGE);
    const size = parsePositiveInt(searchParams.get("size"), DEFAULT_SIZE);

    const { data, isPending, isError, error, isFetching } = useVideosPage(page, size);

    //////////////////
    const { user, logout, isInitializing } = useAuth();

    if (isInitializing) {
        return <div>Loading auth...</div>;
    }
    /////////////////////////////////

    function handlePageChange(nextPage: number) {
        setSearchParams({
            page: String(nextPage),
            size: String(size),
        });
    }

    if (isPending) {
        return <div>Loading videos...</div>;
    }

    if (isError) {
        return (
            <div>
                <p>Failed to load videos.</p>
                <pre>{error instanceof Error ? error.message : "Unknown error"}</pre>
            </div>
        );
    }

    return (
        <main>
            <div style={{ padding: 24 }}>
                <h1>Home</h1>
                {user ? (
                    <>
                        <p>User ID: {user.id}</p>
                        <p>Email: {user.email}</p>
                        <p>Role: {user.role}</p>
                        <button onClick={() => void logout()}>Logout</button>
                    </>
                ) : (
                    <p>Guest</p>
                )}
            </div>
            <header>
                <h1>Latest videos</h1>
                {isFetching && <span>Updating...</span>}
            </header>

            <VideoGrid videos={data?.items ?? []} />
            {/*<VideoGrid videos={data?.items}/>*/}

            <VideoPagination
                page={data?.page}
                totalPages={data?.totalPages}
                onPageChange={handlePageChange}
            />
        </main>
    );
}