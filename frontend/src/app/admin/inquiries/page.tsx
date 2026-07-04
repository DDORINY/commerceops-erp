'use client';

import { useState, useEffect, useCallback } from 'react';
import AdminLayout from '@/components/admin/AdminLayout';
import Pagination from '@/components/common/Pagination';
import Button from '@/components/common/Button';
import { inquiryService, type ApiInquiry } from '@/lib/services/inquiryService';
import {
  formatDateTime,
  INQUIRY_STATUS_LABEL,
  INQUIRY_STATUS_COLOR,
  INQUIRY_TYPE_LABEL,
} from '@/lib/format';

type StatusFilter = 'ALL' | 'WAITING' | 'ANSWERED' | 'CLOSED';

const STATUS_FILTERS: { value: StatusFilter; label: string }[] = [
  { value: 'ALL', label: '전체' },
  { value: 'WAITING', label: '답변 대기' },
  { value: 'ANSWERED', label: '답변 완료' },
  { value: 'CLOSED', label: '종료' },
];

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
  const [expandedId, setExpandedId] = useState<number | null>(null);
  const [answerInput, setAnswerInput] = useState('');

  const fetchInquiries = useCallback(() => {
    inquiryService
      .getAdminInquiries(statusFilter, searchKeyword || undefined, page - 1, PAGE_SIZE)
      .then((res) => {
        setInquiries(res.content);
        setTotalPages(res.totalPages || 1);
        setTotalElements(res.totalElements);
      })
      .catch(() => setInquiries([]))
      .finally(() => setLoading(false));
  }, [statusFilter, searchKeyword, page]);

  useEffect(() => {
    fetchInquiries();
  }, [fetchInquiries]);

  const handleAnswer = async (inquiryId: number) => {
    if (!answerInput.trim()) { alert('답변 내용을 입력하세요.'); return; }
    try {
      await inquiryService.answerInquiry(inquiryId, answerInput.trim());
      setExpandedId(null);
      setAnswerInput('');
      fetchInquiries();
    } catch (err) {
      alert(err instanceof Error ? err.message : '답변 등록에 실패했습니다.');
    }
  };

  const handleClose = async (inquiryId: number) => {
    if (!confirm('문의를 종료 처리하시겠습니까?')) return;
    try {
      await inquiryService.closeInquiry(inquiryId);
      fetchInquiries();
    } catch (err) {
      alert(err instanceof Error ? err.message : '종료 처리에 실패했습니다.');
    }
  };

  return (
    <AdminLayout title="문의 관리">
      <div className="flex flex-wrap gap-2 mb-4">
        {STATUS_FILTERS.map((f) => (
          <button
            key={f.value}
            onClick={() => { setStatusFilter(f.value); setPage(1); }}
            className={[
              'px-4 py-1.5 text-xs font-medium border transition-colors',
              statusFilter === f.value
                ? 'border-[#1a1f2e] bg-[#1a1f2e] text-white'
                : 'border-[#e8eaf0] text-[#8a9bb5] hover:border-[#1a1f2e] hover:text-[#1a1f2e] bg-white',
            ].join(' ')}
          >
            {f.label}
          </button>
        ))}
      </div>

      <div className="bg-white border border-[#e8eaf0] p-4 mb-4 flex gap-3">
        <input
          type="text"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          onKeyDown={(e) => { if (e.key === 'Enter') { setSearchKeyword(keyword); setPage(1); } }}
          placeholder="제목, 고객명 검색"
          className="flex-1 border border-[#e0e0e0] px-3 py-2 text-sm outline-none focus:border-[#1a1f2e]"
        />
        <Button variant="primary" size="sm" onClick={() => { setSearchKeyword(keyword); setPage(1); }}>
          검색
        </Button>
      </div>

      {loading ? (
        <div className="py-12 text-center text-[#bbb] text-sm">로딩 중...</div>
      ) : (
        <div className="bg-white border border-[#e8eaf0]">
          {inquiries.length === 0 ? (
            <div className="py-12 text-center text-[#bbb] text-sm">문의가 없습니다.</div>
          ) : (
            inquiries.map((inquiry) => (
              <div key={inquiry.inquiryId} className="border-b border-[#f0f2f7] last:border-b-0">
                {/* 행 헤더 */}
                <div
                  className="flex items-center gap-4 px-5 py-4 cursor-pointer hover:bg-[#fafbfd] transition-colors"
                  onClick={() => setExpandedId(expandedId === inquiry.inquiryId ? null : inquiry.inquiryId)}
                >
                  <span className={`text-xs font-medium px-2 py-0.5 flex-shrink-0 ${INQUIRY_STATUS_COLOR[inquiry.status] ?? ''}`}>
                    {INQUIRY_STATUS_LABEL[inquiry.status] ?? inquiry.status}
                  </span>
                  <span className="text-xs text-[#8a9bb5] flex-shrink-0">
                    {INQUIRY_TYPE_LABEL[inquiry.type] ?? inquiry.type}
                  </span>
                  <span className="text-sm font-medium text-[#1a1f2e] flex-1 truncate">{inquiry.subject}</span>
                  <span className="text-xs text-[#aaa] flex-shrink-0">{inquiry.userName}</span>
                  <span className="text-xs text-[#aaa] flex-shrink-0">{formatDateTime(inquiry.createdAt)}</span>
                </div>

                {/* 확장 영역 */}
                {expandedId === inquiry.inquiryId && (
                  <div className="px-5 pb-5 bg-[#fafbfd] border-t border-[#f0f2f7]">
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
                          autoFocus={expandedId === inquiry.inquiryId}
                          value={answerInput}
                          onChange={(e) => setAnswerInput(e.target.value)}
                          placeholder="답변 내용을 입력하세요..."
                          rows={3}
                          className="w-full border border-[#e0e0e0] px-3 py-2 text-sm outline-none focus:border-[#1a1f2e] resize-none"
                        />
                        <div className="flex gap-2">
                          <Button variant="primary" size="sm" onClick={() => handleAnswer(inquiry.inquiryId)}>
                            답변 등록
                          </Button>
                        </div>
                      </div>
                    )}

                    {inquiry.status === 'ANSWERED' && (
                      <Button variant="ghost" size="sm" onClick={() => handleClose(inquiry.inquiryId)}>
                        문의 종료
                      </Button>
                    )}
                  </div>
                )}
              </div>
            ))
          )}
        </div>
      )}

      <div className="mt-2 text-xs text-[#aaa]">총 {totalElements}건</div>
      <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
    </AdminLayout>
  );
}
