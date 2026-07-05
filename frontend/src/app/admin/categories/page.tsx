'use client';

import { useEffect, useMemo, useState } from 'react';
import AdminLayout from '@/components/admin/AdminLayout';
import DataTable from '@/components/admin/DataTable';
import Button from '@/components/common/Button';
import Input from '@/components/common/Input';
import {
  categoryService,
  flattenCategoryTree,
  type ApiCategoryNode,
  type CategoryPayload,
} from '@/lib/services/categoryService';

const emptyForm = {
  name: '',
  parentId: '',
  sortOrder: '0',
  active: true,
  visibleInNav: true,
  slug: '',
};

export default function AdminCategoriesPage() {
  const [tree, setTree] = useState<ApiCategoryNode[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [editingId, setEditingId] = useState<number | null>(null);
  const [form, setForm] = useState(emptyForm);

  const categories = useMemo(() => flattenCategoryTree(tree), [tree]);

  const loadCategories = async () => {
    setLoading(true);
    setError('');
    try {
      const res = await categoryService.getAdminCategoryTree();
      setTree(res);
    } catch (err) {
      setTree([]);
      setError(err instanceof Error ? err.message : '카테고리 목록을 불러오지 못했습니다.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    queueMicrotask(() => {
      loadCategories();
    });
  }, []);

  const set = (field: keyof typeof form, value: string | boolean) =>
    setForm((prev) => ({ ...prev, [field]: value }));

  const resetForm = () => {
    setEditingId(null);
    setForm(emptyForm);
    setMessage('');
  };

  const buildPayload = (): CategoryPayload => ({
    name: form.name.trim(),
    parentId: form.parentId ? Number(form.parentId) : null,
    sortOrder: form.sortOrder ? Number(form.sortOrder) : 0,
    active: form.active,
    visibleInNav: form.visibleInNav,
    slug: form.slug.trim() || null,
  });

  const handleEdit = (category: ApiCategoryNode) => {
    setEditingId(category.id);
    setForm({
      name: category.name,
      parentId: category.parentId ? String(category.parentId) : '',
      sortOrder: String(category.sortOrder ?? 0),
      active: category.active,
      visibleInNav: category.visibleInNav,
      slug: category.slug ?? '',
    });
    setMessage('');
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!form.name.trim()) {
      setMessage('카테고리명을 입력해주세요.');
      return;
    }

    setSaving(true);
    setMessage('');
    try {
      const payload = buildPayload();
      if (editingId) {
        await categoryService.updateCategory(editingId, payload);
        setMessage('카테고리가 수정되었습니다.');
      } else {
        await categoryService.createCategory(payload);
        setMessage('카테고리가 생성되었습니다.');
      }
      resetForm();
      await loadCategories();
    } catch (err) {
      setMessage(err instanceof Error ? err.message : '카테고리 저장에 실패했습니다.');
    } finally {
      setSaving(false);
    }
  };

  const parentOptions = categories.filter((category) => category.id !== editingId);

  return (
    <AdminLayout title="카테고리 관리">
      <div className="grid grid-cols-1 xl:grid-cols-[360px_1fr] gap-5">
        <form onSubmit={handleSubmit} className="bg-white border border-[#e8eaf0] p-5 space-y-4">
          <div>
            <h2 className="text-sm font-bold text-[#1a1f2e]">
              {editingId ? '카테고리 수정' : '카테고리 생성'}
            </h2>
            <p className="mt-1 text-xs text-[#8a9bb5]">
              활성 상태이고 네비에 노출되는 카테고리만 쇼핑몰 상단에 표시됩니다.
            </p>
          </div>

          {message && (
            <p className="border border-[#e8eaf0] bg-[#f7f8fc] px-3 py-2 text-xs text-[#4f5b70]">
              {message}
            </p>
          )}

          <Input
            label="이름"
            value={form.name}
            onChange={(e) => set('name', e.target.value)}
            fullWidth
          />

          <div>
            <label className="block text-sm font-medium text-[#444] mb-1">상위 카테고리</label>
            <select
              value={form.parentId}
              onChange={(e) => set('parentId', e.target.value)}
              className="border border-[#ddd] bg-white text-[#222] text-sm px-3 py-2.5 outline-none focus:border-[#222] w-full"
            >
              <option value="">최상위 카테고리</option>
              {parentOptions.map((category) => (
                <option key={category.id} value={category.id}>
                  {'-'.repeat(category.depth)} {category.name}
                </option>
              ))}
            </select>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <Input
              label="정렬 순서"
              type="number"
              value={form.sortOrder}
              onChange={(e) => set('sortOrder', e.target.value)}
              fullWidth
            />
            <Input
              label="슬러그"
              value={form.slug}
              onChange={(e) => set('slug', e.target.value)}
              placeholder="선택 입력"
              fullWidth
            />
          </div>

          <div className="space-y-2">
            <label className="flex items-center gap-2 text-sm text-[#555]">
              <input
                type="checkbox"
                checked={form.active}
                onChange={(e) => set('active', e.target.checked)}
                className="accent-[#222]"
              />
              활성
            </label>
            <label className="flex items-center gap-2 text-sm text-[#555]">
              <input
                type="checkbox"
                checked={form.visibleInNav}
                onChange={(e) => set('visibleInNav', e.target.checked)}
                className="accent-[#222]"
              />
              쇼핑몰 네비에 노출
            </label>
          </div>

          <div className="flex justify-end gap-2">
            {editingId && (
              <Button type="button" variant="outline" onClick={resetForm}>
                취소
              </Button>
            )}
            <Button type="submit" variant="primary" disabled={saving}>
              {saving ? '저장 중...' : editingId ? '수정' : '생성'}
            </Button>
          </div>
        </form>

        <div>
          {loading ? (
            <div className="py-12 text-center text-[#bbb] text-sm">카테고리를 불러오는 중...</div>
          ) : error ? (
            <div className="bg-white border border-[#f1c7c7] px-5 py-12 text-center">
              <p className="text-sm text-[#c43a3a]">{error}</p>
              <Button variant="outline" size="sm" onClick={loadCategories} className="mt-4">
                다시 불러오기
              </Button>
            </div>
          ) : (
            <DataTable<ApiCategoryNode>
              keyField="id"
              data={categories}
              emptyMessage="카테고리가 없습니다."
              columns={[
                {
                  key: 'name',
                  header: '이름',
                  render: (row) => (
                    <span className="font-medium text-[#222]">
                      {'-'.repeat(row.depth)} {row.name}
                    </span>
                  ),
                },
                { key: 'parentId', header: '상위 ID', render: (row) => row.parentId ?? '-' },
                { key: 'sortOrder', header: '정렬' },
                {
                  key: 'active',
                  header: '활성',
                  render: (row) => row.active ? 'Y' : 'N',
                },
                {
                  key: 'visibleInNav',
                  header: '네비',
                  render: (row) => row.visibleInNav ? 'Y' : 'N',
                },
                { key: 'slug', header: '슬러그', render: (row) => row.slug || '-' },
                {
                  key: 'actions',
                  header: '관리',
                  render: (row) => (
                    <Button variant="outline" size="sm" onClick={() => handleEdit(row)}>
                      수정
                    </Button>
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
