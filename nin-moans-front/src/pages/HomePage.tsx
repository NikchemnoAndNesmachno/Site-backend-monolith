import {useEffect, useMemo} from "react";
import {useSearchParams} from "react-router-dom";
import {useVideosPage} from "../hooks/useVideosPage";
import {VideoGrid} from "../components/VideoGrid";
import {VideoPagination} from "../components/VideoPagination";
import useAuth from "../hooks/useAuth.ts";
import type {FeedSort} from "../types/video.ts";
import {AccountPanel} from "../components/home/AccountPanel.tsx";
import {FeedToolbar} from "../components/home/FeedToolbar.tsx";

const DEFAULT_PAGE = 0;
const MIN_PAGE = 0;
const MAX_PAGE = 10_000;

const DEFAULT_SIZE = 20;
const MIN_SIZE = 1;
const MAX_SIZE = 50;

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

type ParsedNumberParam = {
    value: number;
    wasNormalized: boolean;
};

function clampNumber(value: number, min: number, max: number) {
    return Math.min(Math.max(value, min), max);
}

function parseIntParam(
    rawValue: string | null,
    fallback: number,
    min: number,
    max: number,
): ParsedNumberParam {
    if (rawValue === null) {
        return { value: fallback, wasNormalized: false };
    }

    const parsed = Number(rawValue);
    if (!Number.isInteger(parsed)) {
        return { value: fallback, wasNormalized: true };
    }

    const clamped = clampNumber(parsed, min, max);
    return {
        value: clamped,
        wasNormalized: clamped !== parsed,
    };
}

function parseSortParam(rawSort: string | null): { value: FeedSort; wasNormalized: boolean } {
    if (rawSort === null) {
        return { value: DEFAULT_SORT, wasNormalized: false };
    }

    if (rawSort === "POPULAR" || rawSort === "LATEST") {
        return { value: rawSort, wasNormalized: false };
    }

    return { value: DEFAULT_SORT, wasNormalized: true };
}

function buildSearchParams(page: number, size: number, sort: FeedSort) {
    return {
        page: String(page),
        size: String(size),
        sort,
    };
}

export function HomePage() {
    const [searchParams, setSearchParams] = useSearchParams();

    const pageParam = parseIntParam(searchParams.get("page"), DEFAULT_PAGE, MIN_PAGE, MAX_PAGE);
    const sizeParam = parseIntParam(searchParams.get("size"), DEFAULT_SIZE, MIN_SIZE, MAX_SIZE);
    const sortParam = parseSortParam(searchParams.get("sort"));

    const page = pageParam.value;
    const size = sizeParam.value;
    const sort = sortParam.value;

    useEffect(() => {
        if (!pageParam.wasNormalized && !sizeParam.wasNormalized && !sortParam.wasNormalized) {
            return;
        }

        setSearchParams(buildSearchParams(page, size, sort), { replace: true });
    }, [
        page,
        pageParam.wasNormalized,
        setSearchParams,
        size,
        sizeParam.wasNormalized,
        sort,
        sortParam.wasNormalized,
    ]);

    const {data, isPending, isError, error, isFetching} = useVideosPage(page, size, sort);
    const {user, logout} = useAuth();

    const currentSortOption = useMemo(
        () => SORT_OPTIONS.find((option) => option.value === sort) ?? SORT_OPTIONS[1],
        [sort],
    );

    function updateSearchParams(nextPage: number, nextSort = sort) {
        setSearchParams(buildSearchParams(nextPage, size, nextSort));
    }

    function handlePageChange(nextPage: number) {
        updateSearchParams(clampNumber(nextPage, MIN_PAGE, MAX_PAGE));
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

                <AccountPanel user={user!} onLogout={() => void logout()} />
            </section>

            <section className="feed-panel">
                <FeedToolbar
                    description={currentSortOption.description}
                    isFetching={isFetching}
                    sort={sort}
                    sortOptions={SORT_OPTIONS}
                    onSortChange={handleSortChange}
                />

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
