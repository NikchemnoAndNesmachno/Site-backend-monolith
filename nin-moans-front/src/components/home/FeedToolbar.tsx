import type {FeedSort} from "../../types/video.ts";

type SortOption = {
    value: FeedSort;
    label: string;
};

type FeedToolbarProps = {
    description: string;
    isFetching: boolean;
    sort: FeedSort;
    sortOptions: SortOption[];
    onSortChange: (nextSort: FeedSort) => void;
};

export function FeedToolbar({ description, isFetching, sort, sortOptions, onSortChange }: FeedToolbarProps) {
    return (
        <header className="feed-panel__header">
            <div>
                <h2 className="feed-panel__title">Стрічка відео</h2>
                <p className="feed-panel__description">{description}</p>
            </div>

            <div className="feed-panel__actions">
                {isFetching ? <span className="feed-panel__updating">Updating...</span> : null}

                <label className="feed-panel__sort-control">
                    <span className="feed-panel__sort-label">Сортування</span>
                    <select
                        className="feed-panel__sort-select"
                        value={sort}
                        onChange={(event) => onSortChange(event.target.value as FeedSort)}
                    >
                        {sortOptions.map((option) => (
                            <option key={option.value} value={option.value}>
                                {option.label}
                            </option>
                        ))}
                    </select>
                </label>
            </div>
        </header>
    );
}
