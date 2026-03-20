import { useSearchParams } from "react-router-dom";
import { useVideosPage } from "../hooks/useVideosPage";
import { VideoGrid } from "../components/VideoGrid";
import { VideoPagination } from "../components/VideoPagination";
import useAuth from "../hooks/useAuth.ts";
import {useMemo} from "react";
import type {FeedSort} from "../types/video.ts";

const DEFAULT_PAGE = 0;
const DEFAULT_SIZE = 20;
const DEFAULT_SORT: FeedSort = "LATEST";

const SORT_OPTIONS: Array<{ value: FeedSort; label: string; description: string }> = [
    {
        value: "POPULAR",
        label: "Популярне",
        description: "Спочатку ролики з найбільшою кількістю лайків",
    },
    {
        value: "LATEST",
        label: "Останнє",
        description: "Спочатку свіжі публікації",
    },
];

function parsePositiveInt(value: string | null, fallback: number): number {
    if (!value) return fallback;

    const parsed = Number(value);
    if (!Number.isInteger(parsed) || parsed < 0) {
        return fallback;
    }

    return parsed;
}

function parseSort(value: string | null): FeedSort {
    return value === "POPULAR" || value === "LATEST" ? value : DEFAULT_SORT;
}

export function HomePage() {
    const [searchParams, setSearchParams] = useSearchParams();

    const page = parsePositiveInt(searchParams.get("page"), DEFAULT_PAGE);
    const size = parsePositiveInt(searchParams.get("size"), DEFAULT_SIZE);
    const sort = parseSort(searchParams.get("sort"));

    const { data, isPending, isError, error, isFetching } = useVideosPage(page, size, sort);

    //////////////////
    const { user, logout, isInitializing } = useAuth();

    const currentSortOption = useMemo(
        () => SORT_OPTIONS.find((option) => option.value === sort) ?? SORT_OPTIONS[1],
        [sort],
    );

    if (isInitializing) {
        return <div className="home-page__status">Loading auth...</div>;
    }
    /////////////////////////////////

    function updateSearchParams(nextPage: number, nextSort = sort) {
        setSearchParams({
            page: String(nextPage),
            size: String(size),
            sort: nextSort,
        });
    }

    function handlePageChange(nextPage: number) {
        updateSearchParams(nextPage);
    }

    function handleSortChange(nextSort: FeedSort) {
        updateSearchParams(DEFAULT_PAGE, nextSort);
    }

    if (isPending) {
        return <div className="home-page__status">Loading videos...</div>;
    }

    if (isError) {
        return (
            <div className="home-page__status home-page__status--error">
                <p>Failed to load videos.</p>
                <pre>{error instanceof Error ? error.message : "Unknown error"}</pre>
            </div>
        );
    }

    return (
        <main className="home-page">
            <section className="home-page__hero">
                <div className="home-page__hero-content">
                    <span className="home-page__eyebrow">Nin Moans feed</span>
                    <h1 className="home-page__title">Відео, які хочеться дивитись далі</h1>
                    <p className="home-page__subtitle">
                        Досліджуйте свіжі та популярні публікації в красивій стрічці з швидким
                        переключенням сортування.
                    </p>
                </div>

                <aside className="home-page__profile-card">
                    <div className="home-page__profile-header">
                        <span className="home-page__profile-badge">
                            {user ? "Акаунт" : "Гість"}
                        </span>
                        {user ? (
                            <button
                                type="button"
                                className="home-page__logout-button"
                                onClick={() => void logout()}
                            >
                                Logout
                            </button>
                        ) : null}
                    </div>

                    {user ? (
                        <div className="home-page__profile-details">
                            <strong>{user.email}</strong>
                            <span>ID: {user.id}</span>
                            <span>Role: {user.role}</span>
                        </div>
                    ) : (
                        <p className="home-page__guest-copy">
                            Увійдіть в акаунт, щоб взаємодіяти з відео і бачити персональні
                            дані профілю.
                        </p>
                    )}
                </aside>
            </section>

            <section className="feed-panel">
                <header className="feed-panel__header">
                    <div>
                        <h2 className="feed-panel__title">Стрічка відео</h2>
                        <p className="feed-panel__description">{currentSortOption.description}</p>
                    </div>

                    <div className="feed-panel__actions">
                        {isFetching ? <span className="feed-panel__updating">Updating...</span> : null}

                        <label className="feed-panel__sort-control">
                            <span className="feed-panel__sort-label">Сортування</span>
                            <select
                                className="feed-panel__sort-select"
                                value={sort}
                                onChange={(event) => handleSortChange(event.target.value as FeedSort)}
                            >
                                {SORT_OPTIONS.map((option) => (
                                    <option key={option.value} value={option.value}>
                                        {option.label}
                                    </option>
                                ))}
                            </select>
                        </label>
                    </div>
                </header>

                <VideoGrid videos={data?.items ?? []} />

                <VideoPagination
                    page={data?.page}
                    totalPages={data?.totalPages}
                    onPageChange={handlePageChange}
                />
            </section>
        </main>
    );
}