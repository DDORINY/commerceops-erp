'use client';

import { FormEvent, useEffect, useState } from 'react';
import AdminLayout from '@/components/admin/AdminLayout';
import DataTable from '@/components/admin/DataTable';
import Pagination from '@/components/common/Pagination';
import Button from '@/components/common/Button';
import {
  shippingSettingService,
  type ApiShippingMethod,
  type ShippingMethodSaveRequest,
} from '@/lib/services/shippingSettingService';

const PAGE_SIZE = 10;

type ActiveFilter = 'ALL' | 'true' | 'false';

type FormState = {
  code: string;
  name: string;
  carrierId: string;
  defaultFee: string;
  description: string;
  active: boolean;
};

const emptyForm: FormState = {
  code: '',
  name: '',
  carrierId: '',
  defaultFee: '0',
  description: '',
  active: true,
};

export default function AdminShippingMethodsPage() {
  const [items, setItems] = useState<ApiShippingMethod[]>([]);
  const [keyword, setKeyword] = useState('');
  const [searchKeyword, setSearchKeyword] = useState('');
  const [activeFilter, setActiveFilter] = useState<ActiveFilter>('ALL');
  const [carrierIdFilter, setCarrierIdFilter] = useState('');
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [reloadKey, setReloadKey] = useState(0);
  const [formOpen, setFormOpen] = useState(false);
  const [editing, setEditing] = useState<ApiShippingMethod | null>(null);
  const [form, setForm] = useState<FormState>(emptyForm);

  useEffect(() => {
    let mounted = true;
    const load = async () => {
      setLoading(true);
      setError('');
      try {
        const response = await shippingSettingService.getShippingMethods({
          keyword: searchKeyword || undefined,
          carrierId: carrierIdFilter ? Number(carrierIdFilter) : undefined,
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
        setError(err instanceof Error ? err.message : '배송 방법 목록을 불러오지 못했습니다.');
      } finally {
        if (mounted) setLoading(false);
      }
    };
    load();
    return () => {
      mounted = false;
    };
  }, [searchKeyword, activeFilter, carrierIdFilter, page, reloadKey]);

  const openCreateForm = () => {
    setEditing(null);
    setForm(emptyForm);
    setError('');
    setMessage('');
    setFormOpen(true);
  };

  const openEditForm = (method: ApiShippingMethod) => {
    setEditing(method);
    setForm({
      code: method.code,
      name: method.name,
      carrierId: method.carrierId ? String(method.carrierId) : '',
      defaultFee: String(method.defaultFee ?? 0),
      description: method.description ?? '',
      active: method.active,
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
    const defaultFee = Number(form.defaultFee);
    if (!form.code.trim() || !form.name.trim()) {
      setError('코드와 배송 방법명을 입력해주세요.');
      return;
    }
    if (Number.isNaN(defaultFee) || defaultFee < 0) {
      setError('기본 배송비는 0원 이상이어야 합니다.');
      return;
    }
    const payload: ShippingMethodSaveRequest = {
      code: form.code.trim(),
      name: form.name.trim(),
      carrierId: form.carrierId ? Number(form.carrierId) : undefined,
      defaultFee,
      description: normalize(form.description),
      active: form.active,
    };

    setSaving(true);
    setError('');
    setMessage('');
    try {
      if (editing) {
        await shippingSettingService.updateShippingMethod(editing.id, payload);
        setMessage('배송 방법이 수정되었습니다.');
      } else {
        await shippingSettingService.createShippingMethod(payload);
        setMessage('배송 방법이 생성되었습니다.');
      }
      closeForm();
      setReloadKey((prev) => prev + 1);
    } catch (err) {
      setError(err instanceof Error ? err.message : '배송 방법 저장에 실패했습니다.');
    } finally {
      setSaving(false);
    }
  };

  const handleActiveToggle = async (method: ApiShippingMethod) => {
    try {
      await shippingSettingService.updateShippingMethodActive(method.id, !method.active);
      setMessage('배송 방법 활성 상태가 변경되었습니다.');
      setReloadKey((prev) => prev + 1);
    } catch (err) {
      setError(err instanceof Error ? err.message : '배송 방법 활성 상태 변경에 실패했습니다.');
    }
  };

  return (
    <AdminLayout title="배송 방법 관리">
      <div className="flex items-center justify-between gap-3 mb-5">
        <div>
          <h1 className="text-xl font-semibold text-[#1a1f2e]">배송 방법 관리</h1>
          <p className="mt-1 text-sm text-[#6f7a8a]">택배사와 기본 배송비를 포함한 배송 방법 기준 정보를 관리합니다.</p>
        </div>
        <Button variant="primary" size="sm" onClick={openCreateForm}>배송 방법 생성</Button>
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
          placeholder="코드, 배송 방법명, 택배사명 검색"
          className="flex-1 min-w-[220px] border border-[#e0e0e0] px-3 py-2 text-sm outline-none focus:border-[#1a1f2e]"
        />
        <input
          type="number"
          value={carrierIdFilter}
          onChange={(event) => {
            setCarrierIdFilter(event.target.value);
            setPage(1);
          }}
          placeholder="택배사 ID"
          className="w-[120px] border border-[#e0e0e0] px-3 py-2 text-sm outline-none focus:border-[#1a1f2e]"
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
        <div className="py-12 text-center text-[#999] text-sm bg-white border border-[#e8eaf0]">배송 방법 목록을 불러오는 중입니다.</div>
      ) : (
        <>
          <DataTable<ApiShippingMethod>
            keyField="id"
            data={items}
            emptyMessage="등록된 배송 방법이 없습니다."
            columns={[
              { key: 'code', header: '코드' },
              { key: 'name', header: '배송 방법명' },
              { key: 'carrierName', header: '택배사', render: (row) => row.carrierName || '-' },
              { key: 'defaultFee', header: '기본 배송비', render: (row) => `${row.defaultFee.toLocaleString()}원` },
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
              <h2 className="text-base font-semibold text-[#1a1f2e]">{editing ? '배송 방법 수정' : '배송 방법 생성'}</h2>
              <button type="button" onClick={closeForm} className="text-sm text-[#6f7a8a] hover:text-[#1a1f2e]">닫기</button>
            </div>
            <form onSubmit={handleSubmit} className="p-5 space-y-4">
              <input className="w-full border border-[#e0e0e0] px-3 py-2 outline-none focus:border-[#1a1f2e]" placeholder="코드 예: STANDARD" value={form.code} onChange={(event) => setForm((prev) => ({ ...prev, code: event.target.value }))} />
              <input className="w-full border border-[#e0e0e0] px-3 py-2 outline-none focus:border-[#1a1f2e]" placeholder="배송 방법명" value={form.name} onChange={(event) => setForm((prev) => ({ ...prev, name: event.target.value }))} />
              <input type="number" className="w-full border border-[#e0e0e0] px-3 py-2 outline-none focus:border-[#1a1f2e]" placeholder="택배사 ID" value={form.carrierId} onChange={(event) => setForm((prev) => ({ ...prev, carrierId: event.target.value }))} />
              <input type="number" min={0} className="w-full border border-[#e0e0e0] px-3 py-2 outline-none focus:border-[#1a1f2e]" placeholder="기본 배송비" value={form.defaultFee} onChange={(event) => setForm((prev) => ({ ...prev, defaultFee: event.target.value }))} />
              <textarea className="w-full border border-[#e0e0e0] px-3 py-2 outline-none focus:border-[#1a1f2e]" rows={3} placeholder="설명" value={form.description} onChange={(event) => setForm((prev) => ({ ...prev, description: event.target.value }))} />
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
