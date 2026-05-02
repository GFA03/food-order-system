interface PaginationProps {
  currentPage: number; // 0-indexed
  totalPages: number;
  totalElements?: number;
  pageSize?: number;
  pageSizeOptions?: number[];
  onPageChange: (page: number) => void;
  onPageSizeChange?: (size: number) => void;
}

// Beyond this many pages, ellipsis is used to collapse the middle of the range.
const MAX_PAGES_WITHOUT_ELLIPSIS = 7;

function getPageNumbers(current: number, total: number): (number | '…')[] {
  if (total <= MAX_PAGES_WITHOUT_ELLIPSIS) return Array.from({ length: total }, (_, i) => i);

  let start = Math.max(0, current - 2);
  let end = Math.min(total - 1, current + 2);

  // Ensure we always show a window of at least 5 pages
  if (end - start < 4) {
    if (start === 0) end = Math.min(total - 1, 4);
    else start = Math.max(0, total - 5);
  }

  const pages: (number | '…')[] = [];

  if (start > 0) {
    pages.push(0);
    if (start > 1) pages.push('…');
  }

  for (let i = start; i <= end; i++) pages.push(i);

  if (end < total - 1) {
    if (end < total - 2) pages.push('…');
    pages.push(total - 1);
  }

  return pages;
}

export default function Pagination({
  currentPage,
  totalPages,
  totalElements,
  pageSize,
  pageSizeOptions,
  onPageChange,
  onPageSizeChange,
}: PaginationProps) {
  const hasSizeSelector = !!(pageSizeOptions?.length && onPageSizeChange);
  const hasNavigation = totalPages > 1;
  const hasInfo = pageSize != null && totalElements != null;

  if (!hasSizeSelector && !hasNavigation) return null;

  const from = hasInfo ? currentPage * pageSize! + 1 : null;
  const to = hasInfo ? Math.min((currentPage + 1) * pageSize!, totalElements!) : null;

  return (
    <div className="mt-6 space-y-3">
      {/* Page size selector + result count */}
      {(hasSizeSelector || hasInfo) && (
        <div className="flex items-center justify-between gap-4 text-sm text-gray-600 flex-wrap">
          {hasSizeSelector ? (
            <label className="flex items-center gap-2">
              <span>Items per page</span>
              <select
                value={pageSize}
                onChange={(e) => {
                  onPageSizeChange!(Number(e.target.value));
                }}
                className="px-2 py-1 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-400"
                aria-label="Items per page"
              >
                {pageSizeOptions!.map((opt) => (
                  <option key={opt} value={opt}>
                    {opt}
                  </option>
                ))}
              </select>
            </label>
          ) : (
            <span />
          )}

          {hasInfo && (
            <span>
              Showing {from}–{to} of {totalElements}
            </span>
          )}
        </div>
      )}

      {/* Page navigation */}
      {hasNavigation && (
        <nav className="flex items-center justify-center gap-1" aria-label="Pagination">
          <button
            onClick={() => onPageChange(currentPage - 1)}
            disabled={currentPage === 0}
            className="px-3 py-2 text-sm font-medium text-gray-600 hover:text-orange-500 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
            aria-label="Previous page"
          >
            ← Prev
          </button>

          {getPageNumbers(currentPage, totalPages).map((page, idx) =>
            page === '…' ? (
              <span
                key={`ellipsis-${idx}`}
                className="w-9 h-9 flex items-center justify-center text-gray-400 text-sm select-none"
              >
                …
              </span>
            ) : (
              <button
                key={page}
                onClick={() => onPageChange(page as number)}
                aria-current={page === currentPage ? 'page' : undefined}
                className={`w-9 h-9 text-sm font-medium rounded-lg transition-colors ${
                  page === currentPage
                    ? 'bg-orange-500 text-white'
                    : 'text-gray-600 hover:bg-orange-50 hover:text-orange-500'
                }`}
              >
                {(page as number) + 1}
              </button>
            )
          )}

          <button
            onClick={() => onPageChange(currentPage + 1)}
            disabled={currentPage === totalPages - 1}
            className="px-3 py-2 text-sm font-medium text-gray-600 hover:text-orange-500 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
            aria-label="Next page"
          >
            Next →
          </button>
        </nav>
      )}
    </div>
  );
}
