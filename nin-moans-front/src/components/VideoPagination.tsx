type Props = {
    page: number;
    totalPages: number;
    onPageChange: (page: number) => void;
};

export function VideoPagination({ page, totalPages, onPageChange }: Props) {
    if (totalPages <= 1) {
        return null;
    }

    const pages = Array.from({ length: totalPages }, (_, i) => i);

    return (
        <nav aria-label="Video pagination" className="video-pagination">
            <button
                onClick={() => onPageChange(page - 1)}
                disabled={page === 0}
            >
                Previous
            </button>

            {pages.map((p) => (
                <button
                    key={p}
                    onClick={() => onPageChange(p)}
                    disabled={p === page}
                    aria-current={p === page ? "page" : undefined}
                >
                    {p + 1}
                </button>
            ))}

            <button
                onClick={() => onPageChange(page + 1)}
                disabled={page >= totalPages - 1}
            >
                Next
            </button>
        </nav>
    );
}