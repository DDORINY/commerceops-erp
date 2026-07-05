'use client';

import { useEffect, useState } from 'react';
import AdminLayout from '@/components/admin/AdminLayout';
import DataTable from '@/components/admin/DataTable';
import Button from '@/components/common/Button';
import Input from '@/components/common/Input';
import {
  BANNER_POSITION_LABEL,
  bannerService,
  type ApiMainBanner,
  type BannerPosition,
  type MainBannerPayload,
} from '@/lib/services/bannerService';

const emptyForm = {
  title: '',
  subtitle: '',
  description: '',
  imageUrl: '',
  linkUrl: '',
  position: 'MAIN_TOP' as BannerPosition,
  sortOrder: '0',
  active: true,
  startsAt: '',
  endsAt: '',
};

const toDateTimeInput = (value: string | null) => (value ? value.slice(0, 16) : '');
const toPayloadDate = (value: string) => (value ? value : null);
const formatDateTime = (value: string | null) =>
  value ? new Date(value).toLocaleString('ko-KR') : '-';

export default function AdminBannersPage() {
  const [banners, setBanners] = useState<ApiMainBanner[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [editingId, setEditingId] = useState<number | null>(null);
  const [form, setForm] = useState(emptyForm);

  const loadBanners = async () => {
    setLoading(true);
    setError('');
    try {
      setBanners(await bannerService.getAdminBanners());
    } catch (err) {
      setBanners([]);
      setError(err instanceof Error ? err.message : '배너 목록을 불러오지 못했습니다.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    queueMicrotask(() => {
      loadBanners();
    });
  }, []);

  const set = (field: keyof typeof form, value: string | boolean) =>
    setForm((prev) => ({ ...prev, [field]: value }));

  const resetForm = () => {
    setEditingId(null);
    setForm(emptyForm);
    setMessage('');
  };

  const handleEdit = (banner: ApiMainBanner) => {
    setEditingId(banner.id);
    setForm({
      title: banner.title,
      subtitle: banner.subtitle ?? '',
      description: banner.description ?? '',
      imageUrl: banner.imageUrl ?? '',
      linkUrl: banner.linkUrl ?? '',
      position: banner.position,
      sortOrder: String(banner.sortOrder ?? 0),
      active: banner.active,
      startsAt: toDateTimeInput(banner.startsAt),
      endsAt: toDateTimeInput(banner.endsAt),
    });
    setMessage('');
  };

  const buildPayload = (): MainBannerPayload => ({
    title: form.title.trim(),
    subtitle: form.subtitle.trim() || null,
    description: form.description.trim() || null,
    imageUrl: form.imageUrl.trim() || null,
    linkUrl: form.linkUrl.trim() || null,
    position: form.position,
    sortOrder: form.sortOrder ? Number(form.sortOrder) : 0,
    active: form.active,
    startsAt: toPayloadDate(form.startsAt),
    endsAt: toPayloadDate(form.endsAt),
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!form.title.trim()) {
      setMessage('배너 제목을 입력해주세요.');
      return;
    }
    if (form.startsAt && form.endsAt && form.startsAt > form.endsAt) {
      setMessage('노출 시작일은 종료일보다 늦을 수 없습니다.');
      return;
    }

    setSaving(true);
    setMessage('');
    try {
      const payload = buildPayload();
      if (editingId) {
        await bannerService.updateBanner(editingId, payload);
        setMessage('배너가 수정되었습니다.');
      } else {
        await bannerService.createBanner(payload);
        setMessage('배너가 등록되었습니다.');
      }
      resetForm();
      await loadBanners();
    } catch (err) {
      setMessage(err instanceof Error ? err.message : '배너 저장에 실패했습니다.');
    } finally {
      setSaving(false);
    }
  };

  const handleDeactivate = async (banner: ApiMainBanner) => {
    if (!confirm(`"${banner.title}" 배너를 비활성화할까요?`)) return;
    setMessage('');
    try {
      await bannerService.deactivateBanner(banner.id);
      setMessage('배너가 비활성화되었습니다.');
      await loadBanners();
    } catch (err) {
      setMessage(err instanceof Error ? err.message : '배너 비활성화에 실패했습니다.');
    }
  };

  return (
    <AdminLayout title="배너 관리">
      <div className="grid grid-cols-1 xl:grid-cols-[380px_1fr] gap-5">
        <form onSubmit={handleSubmit} className="bg-white border border-[#e8eaf0] p-5 space-y-4">
          <div>
            <h2 className="text-sm font-bold text-[#1a1f2e]">
              {editingId ? '배너 수정' : '배너 등록'}
            </h2>
            <p className="mt-1 text-xs text-[#8a9bb5]">
              활성 상태이고 노출 기간에 포함되는 배너만 사용자 메인에 표시됩니다.
            </p>
          </div>

          {message && (
            <p className="border border-[#e8eaf0] bg-[#f7f8fc] px-3 py-2 text-xs text-[#4f5b70]">
              {message}
            </p>
          )}

          <Input label="제목 *" value={form.title} onChange={(e) => set('title', e.target.value)} placeholder="예: 새 시즌 컬렉션" fullWidth />
          <Input label="부제목" value={form.subtitle} onChange={(e) => set('subtitle', e.target.value)} placeholder="예: 2026 S/S 기획전" fullWidth />
          <div>
            <label className="block text-sm font-medium text-[#444] mb-1">설명</label>
            <textarea value={form.description} onChange={(e) => set('description', e.target.value)} rows={3} className="w-full border border-[#ddd] bg-white text-[#222] text-sm px-3 py-2.5 outline-none focus:border-[#222] resize-none" />
          </div>
          <Input label="이미지 URL" value={form.imageUrl} onChange={(e) => set('imageUrl', e.target.value)} placeholder="/uploads/..." fullWidth />
          <Input label="링크 URL" value={form.linkUrl} onChange={(e) => set('linkUrl', e.target.value)} placeholder="/products" fullWidth />

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-sm font-medium text-[#444] mb-1">노출 위치</label>
              <select value={form.position} onChange={(e) => set('position', e.target.value as BannerPosition)} className="border border-[#ddd] bg-white text-[#222] text-sm px-3 py-2.5 outline-none focus:border-[#222] w-full">
                {(Object.keys(BANNER_POSITION_LABEL) as BannerPosition[]).map((position) => (
                  <option key={position} value={position}>{BANNER_POSITION_LABEL[position]}</option>
                ))}
              </select>
            </div>
            <Input label="정렬 순서" type="number" value={form.sortOrder} onChange={(e) => set('sortOrder', e.target.value)} fullWidth />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <Input label="노출 시작일" type="datetime-local" value={form.startsAt} onChange={(e) => set('startsAt', e.target.value)} fullWidth />
            <Input label="노출 종료일" type="datetime-local" value={form.endsAt} onChange={(e) => set('endsAt', e.target.value)} fullWidth />
          </div>

          <label className="flex items-center gap-2 text-sm text-[#555]">
            <input type="checkbox" checked={form.active} onChange={(e) => set('active', e.target.checked)} className="accent-[#222]" />
            활성
          </label>

          <div className="flex justify-end gap-2">
            {editingId && <Button type="button" variant="outline" onClick={resetForm}>취소</Button>}
            <Button type="submit" variant="primary" disabled={saving}>
              {saving ? '저장 중...' : editingId ? '수정' : '등록'}
            </Button>
          </div>
        </form>

        <div>
          {loading ? (
            <div className="py-12 text-center text-[#bbb] text-sm">배너를 불러오는 중...</div>
          ) : error ? (
            <div className="bg-white border border-[#f1c7c7] px-5 py-12 text-center">
              <p className="text-sm text-[#c43a3a]">{error}</p>
              <Button variant="outline" size="sm" onClick={loadBanners} className="mt-4">다시 불러오기</Button>
            </div>
          ) : (
            <DataTable<ApiMainBanner>
              keyField="id"
              data={banners}
              emptyMessage="등록된 배너가 없습니다."
              columns={[
                {
                  key: 'title',
                  header: '제목',
                  render: (row) => (
                    <div>
                      <p className="font-medium text-[#222]">{row.title}</p>
                      <p className="text-xs text-[#999] mt-0.5">{row.subtitle || '-'}</p>
                    </div>
                  ),
                },
                { key: 'position', header: '위치', render: (row) => BANNER_POSITION_LABEL[row.position] },
                { key: 'sortOrder', header: '정렬' },
                { key: 'active', header: '활성', render: (row) => row.active ? 'Y' : 'N' },
                { key: 'period', header: '노출 기간', render: (row) => `${formatDateTime(row.startsAt)} ~ ${formatDateTime(row.endsAt)}` },
                { key: 'linkUrl', header: '링크', render: (row) => row.linkUrl || '-' },
                {
                  key: 'actions',
                  header: '관리',
                  render: (row) => (
                    <div className="flex gap-2">
                      <Button variant="outline" size="sm" onClick={() => handleEdit(row)}>수정</Button>
                      <Button variant="ghost" size="sm" onClick={() => handleDeactivate(row)} disabled={!row.active}>비활성화</Button>
                    </div>
                  ),
                },
              ]}
            />
          )}
        </div>
      </div>
    </AdminLayout>
  );
}
