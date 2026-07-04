'use client';

interface PaginationProps {
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
}

export default function Pagination({
  currentPage,
  totalPages,
  onPageChange,
}: PaginationProps) {
  if (totalPages <= 1) return null;

  const pages: number[] = [];
  const start = Math.max(1, currentPage - 2);
  const end = Math.min(totalPages, start + 4);

  for (let i = start; i <= end; i++) pages.push(i);

  return (
    <div className="flex items-center justify-center gap-1 mt-8">
      <button
        onClick={() => onPageChange(currentPage - 1)}
        disabled={currentPage === 1}
        className="w-9 h-9 flex items-center justify-center border border-[#e5e5e5] text-[#555] text-sm disabled:opacity-30 hover:border-[#222] hover:text-[#222] transition-colors"
      >
        &lt;
      </button>

      {start > 1 && (
        <>
          <button
            onClick={() => onPageChange(1)}
            className="w-9 h-9 flex items-center justify-center border border-[#e5e5e5] text-sm hover:border-[#222] hover:text-[#222] transition-colors"
          >
            1
          </button>
          {start > 2 && <span className="px-1 text-[#bbb]">…</span>}
        </>
      )}

      {pages.map((page) => (
        <button
          key={page}
          onClick={() => onPageChange(page)}
          className={[
            'w-9 h-9 flex items-center justify-center border text-sm transition-colors',
            page === currentPage
              ? 'border-[#222] bg-[#222] text-white'
              : 'border-[#e5e5e5] text-[#555] hover:border-[#222] hover:text-[#222]',
          ].join(' ')}
        >
          {page}
        </button>
      ))}

      {end < totalPages && (
        <>
          {end < totalPages - 1 && <span className="px-1 text-[#bbb]">…</span>}
          <button
            onClick={() => onPageChange(totalPages)}
            className="w-9 h-9 flex items-center justify-center border border-[#e5e5e5] text-sm hover:border-[#222] hover:text-[#222] transition-colors"
          >
            {totalPages}
          </button>
        </>
      )}

      <button
        onClick={() => onPageChange(currentPage + 1)}
        disabled={currentPage === totalPages}
        className="w-9 h-9 flex items-center justify-center border border-[#e5e5e5] text-[#555] text-sm disabled:opacity-30 hover:border-[#222] hover:text-[#222] transition-colors"
      >
        &gt;
      </button>
    </div>
  );
}
