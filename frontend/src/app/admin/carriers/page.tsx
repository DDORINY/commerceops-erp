'use client';

import { FormEvent, useEffect, useState } from 'react';
import AdminLayout from '@/components/admin/AdminLayout';
import DataTable from '@/components/admin/DataTable';
import Pagination from '@/components/common/Pagination';
import Button from '@/components/common/Button';
import { shippingSettingService, type ApiCarrier, type CarrierSaveRequest } from '@/lib/services/shippingSettingService';

const PAGE_SIZE = 10;

type ActiveFilter = 'ALL' | 'true' | 'false';

type FormState = {
  code: string;
  name: string;
  trackingUrlTemplate: string;
  active: boolean;
};

const emptyForm: FormState = {
  code: '',
  name: '',
  trackingUrlTemplate: '',
  active: true,
};

export default function AdminCarriersPage() {
  const [items, setItems] = useState<ApiCarrier[]>([]);
  const [keyword, setKeyword] = useState('');
  const [searchKeyword, setSearchKeyword] = useState('');
  const [activeFilter, setActiveFilter] = useState<ActiveFilter>('ALL');
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [reloadKey, setReloadKey] = useState(0);
  const [formOpen, setFormOpen] = useState(false);
  const [editing, setEditing] = useState<ApiCarrier | null>(null);
  const [form, setForm] = useState<FormState>(emptyForm);

  useEffect(() => {
    let mounted = true;
    const load = async () => {
      setLoading(true);
      setError('');
      try {
        const response = await shippingSettingService.getCarriers({
          keyword: searchKeyword || undefined,
          active: activeFilter === 'ALL' ? 'ALL' : activeFilter === 'true',
          page: page - 1,
          size: PAGE_SIZE,
        });
        if (!mounted) return;
        setItems(response.content);
        setTotalPages(response.totalPages || 1);
        setTotalElements(response.totalElements);
      } catch (err) {
        if (!mounted) return;
        setItems([]);
        setTotalPages(1);
        setTotalElements(0);
        setError(err instanceof Error ? err.message : '택배사 목록을 불러오지 못했습니다.');
      } finally {
        if (mounted) setLoading(false);
      }
    };
    load();
    return () => {
      mounted = false;
    };
  }, [searchKeyword, activeFilter, page, reloadKey]);

  const openCreateForm = () => {
    setEditing(null);
    setForm(emptyForm);
    setError('');
    setMessage('');
    setFormOpen(true);
  };

  const openEditForm = (carrier: ApiCarrier) => {
    setEditing(carrier);
    setForm({
      code: carrier.code,
      name: carrier.name,
      trackingUrlTemplate: carrier.trackingUrlTemplate ?? '',
      active: carrier.active,
    });
    setError('');
    setMessage('');
    setFormOpen(true);
  };

  const closeForm = () => {
    if (saving) return;
    setFormOpen(false);
    setEditing(null);
    setForm(emptyForm);
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!form.code.trim() || !form.name.trim()) {
      setError('코드와 택배사명을 입력해주세요.');
      return;
    }
    const payload: CarrierSaveRequest = {
      code: form.code.trim(),
      name: form.name.trim(),
      trackingUrlTemplate: normalize(form.trackingUrlTemplate),
      active: form.active,
    };

    setSaving(true);
    setError('');
    setMessage('');
    try {
      if (editing) {
        await shippingSettingService.updateCarrier(editing.id, payload);
        setMessage('택배사가 수정되었습니다.');
      } else {
        await shippingSettingService.createCarrier(payload);
        setMessage('택배사가 생성되었습니다.');
      }
      closeForm();
      setReloadKey((prev) => prev + 1);
    } catch (err) {
      setError(err instanceof Error ? err.message : '택배사 저장에 실패했습니다.');
    } finally {
      setSaving(false);
    }
  };

  const handleActiveToggle = async (carrier: ApiCarrier) => {
    try {
      await shippingSettingService.updateCarrierActive(carrier.id, !carrier.active);
      setMessage('택배사 활성 상태가 변경되었습니다.');
      setReloadKey((prev) => prev + 1);
    } catch (err) {
      setError(err instanceof Error ? err.message : '택배사 활성 상태 변경에 실패했습니다.');
    }
  };

  return (
    <AdminLayout title="택배사 관리">
      <div className="flex items-center justify-between gap-3 mb-5">
        <div>
          <h1 className="text-xl font-semibold text-[#1a1f2e]">택배사 관리</h1>
          <p className="mt-1 text-sm text-[#6f7a8a]">송장과 배송 처리에서 사용할 택배사 기준 정보를 관리합니다.</p>
        </div>
        <Button variant="primary" size="sm" onClick={openCreateForm}>택배사 생성</Button>
      </div>

      <div className="bg-white border border-[#e8eaf0] p-4 mb-4 flex flex-wrap gap-3">
        <input
          value={keyword}
          onChange={(event) => setKeyword(event.target.value)}
          onKeyDown={(event) => {
            if (event.key === 'Enter') {
              setSearchKeyword(keyword);
              setPage(1);
            }
          }}
          placeholder="코드 또는 택배사명 검색"
          className="flex-1 min-w-[220px] border border-[#e0e0e0] px-3 py-2 text-sm outline-none focus:border-[#1a1f2e]"
        />
        <select
          value={activeFilter}
          onChange={(event) => {
            setActiveFilter(event.target.value as ActiveFilter);
            setPage(1);
          }}
          className="border border-[#e0e0e0] px-3 py-2 text-sm outline-none bg-white"
        >
          <option value="ALL">전체 상태</option>
          <option value="true">활성</option>
          <option value="false">비활성</option>
        </select>
        <Button variant="outline" size="sm" onClick={() => { setSearchKeyword(keyword); setPage(1); }}>검색</Button>
      </div>

      {message && <div className="mb-3 border border-[#b7dfc1] bg-[#f0fff4] px-4 py-3 text-sm text-[#246b38]">{message}</div>}
      {error && <div className="mb-3 border border-[#f1c7c7] bg-[#fff6f6] px-4 py-3 text-sm text-[#c43a3a]">{error}</div>}

      {loading ? (
        <div className="py-12 text-center text-[#999] text-sm bg-white border border-[#e8eaf0]">택배사 목록을 불러오는 중입니다.</div>
      ) : (
        <>
          <DataTable<ApiCarrier>
            keyField="id"
            data={items}
            emptyMessage="등록된 택배사가 없습니다."
            columns={[
              { key: 'code', header: '코드' },
              { key: 'name', header: '택배사명' },
              { key: 'trackingUrlTemplate', header: '추적 URL 템플릿', render: (row) => row.trackingUrlTemplate || '-' },
              { key: 'active', header: '상태', render: (row) => row.active ? '활성' : '비활성' },
              {
                key: 'actions',
                header: '관리',
                render: (row) => (
                  <div className="flex gap-2">
                    <Button variant="outline" size="sm" onClick={() => openEditForm(row)}>수정</Button>
                    <Button variant="ghost" size="sm" onClick={() => handleActiveToggle(row)}>
                      {row.active ? '비활성' : '활성'}
                    </Button>
                  </div>
                ),
              },
            ]}
          />
          <div className="mt-2 text-xs text-[#8a9bb5]">총 {totalElements}건</div>
          <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
        </>
      )}

      {formOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/30 px-4">
          <div className="w-full max-w-[560px] bg-white border border-[#dfe3ea] shadow-xl">
            <div className="flex items-center justify-between border-b border-[#edf0f5] px-5 py-4">
              <h2 className="text-base font-semibold text-[#1a1f2e]">{editing ? '택배사 수정' : '택배사 생성'}</h2>
              <button type="button" onClick={closeForm} className="text-sm text-[#6f7a8a] hover:text-[#1a1f2e]">닫기</button>
            </div>
            <form onSubmit={handleSubmit} className="p-5 space-y-4">
              <input className="w-full border border-[#e0e0e0] px-3 py-2 outline-none focus:border-[#1a1f2e]" placeholder="코드 예: CJ" value={form.code} onChange={(event) => setForm((prev) => ({ ...prev, code: event.target.value }))} />
              <input className="w-full border border-[#e0e0e0] px-3 py-2 outline-none focus:border-[#1a1f2e]" placeholder="택배사명" value={form.name} onChange={(event) => setForm((prev) => ({ ...prev, name: event.target.value }))} />
              <input className="w-full border border-[#e0e0e0] px-3 py-2 outline-none focus:border-[#1a1f2e]" placeholder="추적 URL 템플릿 예: https://.../{trackingNumber}" value={form.trackingUrlTemplate} onChange={(event) => setForm((prev) => ({ ...prev, trackingUrlTemplate: event.target.value }))} />
              <label className="flex items-center gap-2 text-sm text-[#566171]">
                <input type="checkbox" checked={form.active} onChange={(event) => setForm((prev) => ({ ...prev, active: event.target.checked }))} />
                활성 상태
              </label>
              <div className="flex justify-end gap-2 pt-2">
                <Button type="button" variant="ghost" size="sm" onClick={closeForm} disabled={saving}>취소</Button>
                <Button type="submit" variant="primary" size="sm" disabled={saving}>{saving ? '저장 중' : '저장'}</Button>
              </div>
            </form>
          </div>
        </div>
      )}
    </AdminLayout>
  );
}

function normalize(value: string) {
  const trimmed = value.trim();
  return trimmed ? trimmed : undefined;
}
