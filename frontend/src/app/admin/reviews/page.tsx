'use client';

import { useEffect, useState } from 'react';
import AdminLayout from '@/components/admin/AdminLayout';
import DataTable from '@/components/admin/DataTable';
import Pagination from '@/components/common/Pagination';
import Button from '@/components/common/Button';
import { reviewService, type ApiReview } from '@/lib/services/reviewService';
import { auditService, type ApiAuditLog } from '@/lib/services/auditService';
import { formatDateTime } from '@/lib/format';

type RatingFilter = 'ALL' | 1 | 2 | 3 | 4 | 5;

const PAGE_SIZE = 15;

const RATING_FILTERS: { value: RatingFilter; label: string }[] = [
  { value: 'ALL', label: '전체' },
  { value: 5, label: '5점' },
  { value: 4, label: '4점' },
  { value: 3, label: '3점' },
  { value: 2, label: '2점' },
  { value: 1, label: '1점' },
];

function renderRating(rating: number): string {
  return `${'★'.repeat(rating)}${'☆'.repeat(5 - rating)} (${rating})`;
}

export default function AdminReviewsPage() {
  const [ratingFilter, setRatingFilter] = useState<RatingFilter>('ALL');
  const [keyword, setKeyword] = useState('');
  const [searchKeyword, setSearchKeyword] = useState('');
  const [page, setPage] = useState(1);
  const [reviews, setReviews] = useState<ApiReview[]>([]);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [reloadKey, setReloadKey] = useState(0);
  const [deletingId, setDeletingId] = useState<number | null>(null);
  const [changingId, setChangingId] = useState<number | null>(null);
  const [message, setMessage] = useState('');
  const [auditLogs, setAuditLogs] = useState<ApiAuditLog[]>([]);

  useEffect(() => {
    let mounted = true;

    const loadReviews = async () => {
      setLoading(true);
      setError('');

      try {
        const res = await reviewService.getAdminReviews(
          ratingFilter,
          searchKeyword || undefined,
          page - 1,
          PAGE_SIZE
        );
        if (!mounted) return;
        setReviews(res.content);
        setTotalPages(res.totalPages || 1);
        setTotalElements(res.totalElements);
      } catch (err) {
        if (!mounted) return;
        setReviews([]);
        setTotalPages(1);
        setTotalElements(0);
        setError(err instanceof Error ? err.message : '리뷰 목록을 불러오지 못했습니다.');
      } finally {
        if (mounted) setLoading(false);
      }
    };

    loadReviews();

    return () => {
      mounted = false;
    };
  }, [ratingFilter, searchKeyword, page, reloadKey]);

  useEffect(() => {
    let mounted = true;
    auditService.getAuditLogs('REVIEW', 0, 8)
      .then((res) => {
        if (mounted) setAuditLogs(res.content);
      })
      .catch(() => {
        if (mounted) setAuditLogs([]);
      });
    return () => {
      mounted = false;
    };
  }, [reloadKey]);

  const handleSearch = () => {
    setSearchKeyword(keyword.trim());
    setPage(1);
  };

  const handleDelete = async (reviewId: number) => {
    if (!confirm('이 리뷰를 삭제하시겠습니까?')) return;

    setDeletingId(reviewId);
    try {
      await reviewService.deleteAdminReview(reviewId);
      setReviews((prev) => prev.filter((review) => review.reviewId !== reviewId));
      setTotalElements((prev) => Math.max(0, prev - 1));
    } catch (err) {
      alert(err instanceof Error ? err.message : '리뷰 삭제에 실패했습니다.');
    } finally {
      setDeletingId(null);
    }
  };

  const handleVisibilityChange = async (review: ApiReview) => {
    const nextAction = review.status === 'HIDDEN' ? 'show' : 'hide';
    setChangingId(review.reviewId);
    setMessage('');
    try {
      if (nextAction === 'hide') {
        await reviewService.hideAdminReview(review.reviewId);
      } else {
        await reviewService.showAdminReview(review.reviewId);
      }
      const nextStatus = nextAction === 'hide' ? 'HIDDEN' : 'VISIBLE';
      setReviews((prev) => prev.map((item) =>
        item.reviewId === review.reviewId ? { ...item, status: nextStatus } : item
      ));
      setReloadKey((prev) => prev + 1);
      setMessage(nextAction === 'hide' ? 'Review has been hidden.' : 'Review has been shown.');
    } catch (err) {
      setMessage(err instanceof Error ? err.message : 'Review status update failed.');
    } finally {
      setChangingId(null);
    }
  };

  return (
    <AdminLayout title="리뷰 관리">
      <div className="flex flex-wrap gap-2 mb-4">
        {RATING_FILTERS.map((filter) => (
          <button
            key={filter.value}
            onClick={() => {
              setRatingFilter(filter.value);
              setPage(1);
            }}
            className={[
              'px-4 py-1.5 text-xs font-medium border transition-colors',
              ratingFilter === filter.value
                ? 'border-[#1a1f2e] bg-[#1a1f2e] text-white'
                : 'border-[#e8eaf0] text-[#8a9bb5] hover:border-[#1a1f2e] hover:text-[#1a1f2e] bg-white',
            ].join(' ')}
          >
            {filter.label}
          </button>
        ))}
      </div>

      <div className="bg-white border border-[#e8eaf0] p-4 mb-4 flex gap-3">
        <input
          type="text"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === 'Enter') handleSearch();
          }}
          placeholder="상품명, 작성자명, 내용 검색"
          className="flex-1 border border-[#e0e0e0] px-3 py-2 text-sm outline-none focus:border-[#1a1f2e]"
        />
        <Button variant="primary" size="sm" onClick={handleSearch}>
          검색
        </Button>
      </div>

      {message && (
        <div className="mb-4 border border-[#d8dce6] bg-white px-4 py-3 text-sm text-[#4f5b70]">
          {message}
        </div>
      )}

      {loading ? (
        <div className="py-12 text-center text-[#bbb] text-sm">로딩 중...</div>
      ) : error ? (
        <div className="bg-white border border-[#f1c7c7] px-5 py-12 text-center">
          <p className="text-sm text-[#c43a3a]">{error}</p>
          <Button variant="outline" size="sm" onClick={() => setReloadKey((prev) => prev + 1)} className="mt-4">
            다시 불러오기
          </Button>
        </div>
      ) : (
        <>
          <DataTable<ApiReview>
            keyField="reviewId"
            data={reviews}
            emptyMessage="리뷰가 없습니다."
            columns={[
              {
                key: 'reviewId',
                header: '리뷰 ID',
                render: (review) => <span className="font-mono text-xs">#{review.reviewId}</span>,
              },
              {
                key: 'productName',
                header: '상품',
                render: (review) => (
                  <div>
                    <p className="font-medium text-[#1a1f2e]">{review.productName}</p>
                    <p className="text-xs text-[#8a9bb5]">상품 ID {review.productId}</p>
                  </div>
                ),
              },
              {
                key: 'userName',
                header: '작성자',
              },
              {
                key: 'rating',
                header: '평점',
                render: (review) => <span className="text-xs text-[#f59e0b]">{renderRating(review.rating)}</span>,
              },
              {
                key: 'content',
                header: '내용',
                render: (review) => (
                  <p className="max-w-[360px] truncate text-[#555]" title={review.content ?? undefined}>
                    {review.content || '-'}
                  </p>
                ),
              },
              {
                key: 'createdAt',
                header: '작성일',
                render: (review) => <span className="text-xs text-[#777]">{formatDateTime(review.createdAt)}</span>,
              },
              {
                key: 'status',
                header: 'Status',
                render: (review) => (
                  <span className={[
                    'text-xs font-medium px-2 py-0.5',
                    review.status === 'HIDDEN' ? 'bg-[#fff7e6] text-[#b45309]' : 'bg-[#eef8f1] text-[#15803d]',
                  ].join(' ')}>
                    {review.status}
                  </span>
                ),
              },
              {
                key: 'actions',
                header: 'Moderation',
                render: (review) => (
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => handleVisibilityChange(review)}
                    disabled={changingId === review.reviewId || deletingId === review.reviewId}
                  >
                    {changingId === review.reviewId
                      ? 'Saving...'
                      : review.status === 'HIDDEN' ? 'Show' : 'Hide'}
                  </Button>
                ),
              },
              {
                key: 'dangerActions',
                header: '',
                className: 'text-right',
                render: (review) => (
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => handleDelete(review.reviewId)}
                    disabled={deletingId === review.reviewId}
                    className="text-[#d94f4f] hover:text-[#c43a3a]"
                  >
                    {deletingId === review.reviewId ? '삭제 중...' : '삭제'}
                  </Button>
                ),
              },
            ]}
          />
          <div className="mt-2 text-xs text-[#aaa]">총 {totalElements}건</div>
          <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
        </>
      )}

      <div className="mt-8 bg-white border border-[#e8eaf0] p-5">
        <h2 className="text-sm font-bold text-[#1a1f2e] mb-4">Recent review audit logs</h2>
        {auditLogs.length === 0 ? (
          <p className="py-6 text-center text-sm text-[#8a9bb5]">No audit logs.</p>
        ) : (
          <DataTable<ApiAuditLog>
            keyField="id"
            data={auditLogs}
            emptyMessage="No audit logs."
            columns={[
              { key: 'actionType', header: 'Action' },
              {
                key: 'targetId',
                header: 'Target',
                render: (log) => `#${log.targetId}`,
              },
              {
                key: 'actorEmail',
                header: 'Actor',
                render: (log) => (
                  <div>
                    <p className="text-sm text-[#1a1f2e]">{log.actorName}</p>
                    <p className="text-xs text-[#8a9bb5]">{log.actorEmail}</p>
                  </div>
                ),
              },
              {
                key: 'afterStatus',
                header: 'Change',
                render: (log) => `${log.beforeStatus ?? '-'} -> ${log.afterStatus ?? '-'}`,
              },
              {
                key: 'createdAt',
                header: 'Created',
                render: (log) => <span className="text-xs text-[#777]">{formatDateTime(log.createdAt)}</span>,
              },
            ]}
          />
        )}
      </div>
    </AdminLayout>
  );
}
