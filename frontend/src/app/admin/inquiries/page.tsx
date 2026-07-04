'use client';

import { useEffect, useState } from 'react';
import AdminLayout from '@/components/admin/AdminLayout';
import Pagination from '@/components/common/Pagination';
import Button from '@/components/common/Button';
import { inquiryService, type ApiInquiry, type InquiryStatus, type InquiryType } from '@/lib/services/inquiryService';
import { formatDateTime } from '@/lib/format';

type StatusFilter = 'ALL' | InquiryStatus;

const STATUS_FILTERS: { value: StatusFilter; label: string }[] = [
  { value: 'ALL', label: '전체' },
  { value: 'WAITING', label: '답변 대기' },
  { value: 'ANSWERED', label: '답변 완료' },
  { value: 'CLOSED', label: '종료' },
];

const INQUIRY_STATUS_LABEL: Record<InquiryStatus, string> = {
  WAITING: '답변 대기',
  ANSWERED: '답변 완료',
  CLOSED: '종료',
};

const INQUIRY_STATUS_COLOR: Record<InquiryStatus, string> = {
  WAITING: 'bg-yellow-100 text-yellow-700',
  ANSWERED: 'bg-green-100 text-green-700',
  CLOSED: 'bg-gray-100 text-gray-500',
};

const INQUIRY_TYPE_LABEL: Record<InquiryType, string> = {
  PRODUCT: '상품 문의',
  ORDER: '주문 문의',
  OTHER: '기타 문의',
};

const PAGE_SIZE = 15;

export default function AdminInquiriesPage() {
  const [statusFilter, setStatusFilter] = useState<StatusFilter>('ALL');
  const [keyword, setKeyword] = useState('');
  const [searchKeyword, setSearchKeyword] = useState('');
  const [page, setPage] = useState(1);
  const [inquiries, setInquiries] = useState<ApiInquiry[]>([]);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [reloadKey, setReloadKey] = useState(0);
  const [expandedId, setExpandedId] = useState<number | null>(null);
  const [answerInput, setAnswerInput] = useState('');
  const [answeringId, setAnsweringId] = useState<number | null>(null);
  const [closingId, setClosingId] = useState<number | null>(null);

  useEffect(() => {
    let mounted = true;

    const loadInquiries = async () => {
      setLoading(true);
      setError('');

      try {
        const res = await inquiryService.getAdminInquiries(statusFilter, searchKeyword || undefined, page - 1, PAGE_SIZE);
        if (!mounted) return;
        setInquiries(res.content);
        setTotalPages(res.totalPages || 1);
        setTotalElements(res.totalElements);
      } catch (err) {
        if (!mounted) return;
        setInquiries([]);
        setTotalPages(1);
        setTotalElements(0);
        setError(err instanceof Error ? err.message : '문의 목록을 불러오지 못했습니다.');
      } finally {
        if (mounted) setLoading(false);
      }
    };

    loadInquiries();

    return () => {
      mounted = false;
    };
  }, [statusFilter, searchKeyword, page, reloadKey]);

  const handleExpand = (inquiryId: number) => {
    setExpandedId((current) => (current === inquiryId ? null : inquiryId));
    setAnswerInput('');
  };

  const handleSearch = () => {
    setSearchKeyword(keyword.trim());
    setPage(1);
  };

  const handleAnswer = async (inquiryId: number) => {
    if (!answerInput.trim()) {
      alert('답변 내용을 입력해주세요.');
      return;
    }

    setAnsweringId(inquiryId);
    try {
      const updated = await inquiryService.answerInquiry(inquiryId, answerInput.trim());
      setInquiries((prev) => prev.map((item) => (item.inquiryId === inquiryId ? updated : item)));
      setAnswerInput('');
    } catch (err) {
      alert(err instanceof Error ? err.message : '답변 등록에 실패했습니다.');
    } finally {
      setAnsweringId(null);
    }
  };

  const handleClose = async (inquiryId: number) => {
    if (!confirm('문의를 종료 처리하시겠습니까?')) return;

    setClosingId(inquiryId);
    try {
      const updated = await inquiryService.closeInquiry(inquiryId);
      setInquiries((prev) => prev.map((item) => (item.inquiryId === inquiryId ? updated : item)));
    } catch (err) {
      alert(err instanceof Error ? err.message : '문의 종료 처리에 실패했습니다.');
    } finally {
      setClosingId(null);
    }
  };

  return (
    <AdminLayout title="문의 관리">
      <div className="flex flex-wrap gap-2 mb-4">
        {STATUS_FILTERS.map((filter) => (
          <button
            key={filter.value}
            onClick={() => {
              setStatusFilter(filter.value);
              setPage(1);
              setExpandedId(null);
              setAnswerInput('');
            }}
            className={[
              'px-4 py-1.5 text-xs font-medium border transition-colors',
              statusFilter === filter.value
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
          placeholder="제목 또는 고객명 검색"
          className="flex-1 border border-[#e0e0e0] px-3 py-2 text-sm outline-none focus:border-[#1a1f2e]"
        />
        <Button variant="primary" size="sm" onClick={handleSearch}>
          검색
        </Button>
      </div>

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
        <div className="bg-white border border-[#e8eaf0]">
          {inquiries.length === 0 ? (
            <div className="py-12 text-center text-[#bbb] text-sm">문의가 없습니다.</div>
          ) : (
            inquiries.map((inquiry) => (
              <div key={inquiry.inquiryId} className="border-b border-[#f0f2f7] last:border-b-0">
                <button
                  type="button"
                  className="w-full flex items-center gap-4 px-5 py-4 text-left hover:bg-[#fafbfd] transition-colors"
                  onClick={() => handleExpand(inquiry.inquiryId)}
                >
                  <span className={`text-xs font-medium px-2 py-0.5 flex-shrink-0 ${INQUIRY_STATUS_COLOR[inquiry.status]}`}>
                    {INQUIRY_STATUS_LABEL[inquiry.status]}
                  </span>
                  <span className="text-xs text-[#8a9bb5] flex-shrink-0">
                    {INQUIRY_TYPE_LABEL[inquiry.type]}
                  </span>
                  <span className="text-sm font-medium text-[#1a1f2e] flex-1 truncate">{inquiry.subject}</span>
                  <span className="hidden md:inline text-xs text-[#aaa] flex-shrink-0">{inquiry.userName}</span>
                  <span className="hidden lg:inline text-xs text-[#aaa] flex-shrink-0">{formatDateTime(inquiry.createdAt)}</span>
                </button>

                {expandedId === inquiry.inquiryId && (
                  <div className="px-5 pb-5 bg-[#fafbfd] border-t border-[#f0f2f7]">
                    <div className="py-3 text-xs text-[#8a9bb5] flex flex-wrap gap-x-4 gap-y-1">
                      <span>고객: {inquiry.userName}</span>
                      {inquiry.productName && <span>상품: {inquiry.productName}</span>}
                      <span>등록일: {formatDateTime(inquiry.createdAt)}</span>
                    </div>
                    <div className="py-4 text-sm text-[#333] whitespace-pre-wrap">{inquiry.content}</div>

                    {inquiry.answer && (
                      <div className="bg-[#f0f4ff] border-l-4 border-[#4c74e5] p-4 mb-4 text-sm text-[#333] whitespace-pre-wrap">
                        <span className="block text-xs font-bold text-[#4c74e5] mb-1">관리자 답변</span>
                        {inquiry.answer}
                      </div>
                    )}

                    {inquiry.status === 'WAITING' && (
                      <div className="flex flex-col gap-2">
                        <textarea
                          value={answerInput}
                          onChange={(e) => setAnswerInput(e.target.value)}
                          placeholder="답변 내용을 입력해주세요."
                          rows={3}
                          className="w-full border border-[#e0e0e0] px-3 py-2 text-sm outline-none focus:border-[#1a1f2e] resize-none"
                        />
                        <div className="flex gap-2">
                          <Button
                            variant="primary"
                            size="sm"
                            onClick={() => handleAnswer(inquiry.inquiryId)}
                            disabled={answeringId === inquiry.inquiryId}
                          >
                            {answeringId === inquiry.inquiryId ? '등록 중...' : '답변 등록'}
                          </Button>
                        </div>
                      </div>
                    )}

                    {inquiry.status === 'ANSWERED' && (
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => handleClose(inquiry.inquiryId)}
                        disabled={closingId === inquiry.inquiryId}
                      >
                        {closingId === inquiry.inquiryId ? '처리 중...' : '문의 종료'}
                      </Button>
                    )}
                  </div>
                )}
              </div>
            ))
          )}
        </div>
      )}

      {!loading && !error && (
        <>
          <div className="mt-2 text-xs text-[#aaa]">총 {totalElements}건</div>
          <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
        </>
      )}
    </AdminLayout>
  );
}
