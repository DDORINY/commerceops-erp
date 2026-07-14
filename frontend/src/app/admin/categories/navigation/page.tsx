'use client';

import { useEffect, useMemo, useState } from 'react';
import AdminLayout from '@/components/admin/AdminLayout';
import Button from '@/components/common/Button';
import DataTable from '@/components/admin/DataTable';
import {
  categoryService,
  flattenCategoryTree,
  type ApiCategoryNode,
} from '@/lib/services/categoryService';

type Draft = { sortOrder: string; slug: string };

export default function AdminCategoryNavigationPage() {
  const [tree, setTree] = useState<ApiCategoryNode[]>([]);
  const [drafts, setDrafts] = useState<Record<number, Draft>>({});
  const [loading, setLoading] = useState(true);
  const [savingId, setSavingId] = useState<number | null>(null);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const categories = useMemo(() => flattenCategoryTree(tree), [tree]);
  const visibleCategories = categories.filter((item) => item.active && item.visibleInNav);

  const load = async () => {
    setError('');
    try {
      const data = await categoryService.getAdminCategoryTree();
      setTree(data);
      setDrafts(Object.fromEntries(flattenCategoryTree(data).map((item) => [item.id, {
        sortOrder: String(item.sortOrder),
        slug: item.slug ?? '',
      }])));
    } catch (err) {
      setError(err instanceof Error ? err.message : '카테고리를 불러오지 못했습니다.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { queueMicrotask(load); }, []);

  const save = async (category: ApiCategoryNode, visibleInNav = category.visibleInNav) => {
    const draft = drafts[category.id] ?? { sortOrder: String(category.sortOrder), slug: category.slug ?? '' };
    setSavingId(category.id);
    setError('');
    setMessage('');
    try {
      await categoryService.updateCategory(category.id, {
        name: category.name,
        parentId: category.parentId,
        sortOrder: Number(draft.sortOrder) || 0,
        active: category.active,
        visibleInNav,
        slug: draft.slug.trim() || null,
      });
      setMessage(`${category.name} 네비 설정을 저장했습니다.`);
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : '네비 설정 저장에 실패했습니다.');
    } finally {
      setSavingId(null);
    }
  };

  return (
    <AdminLayout title="상단 네비 관리">
      <section className="mb-5 border border-[#e8eaf0] bg-white p-5">
        <h2 className="text-base font-semibold text-[#1a1f2e]">쇼핑몰 상단 네비게이션</h2>
        <p className="mt-1 text-sm text-[#6f7a8a]">카테고리별 메뉴 노출 여부, 표시 순서와 URL 슬러그를 관리합니다.</p>
        <div className="mt-4 flex flex-wrap gap-2">
          {visibleCategories.length === 0 ? <span className="text-xs text-[#9aa3b1]">노출 중인 메뉴가 없습니다.</span> : visibleCategories.map((item) => (
            <span key={item.id} className="border border-[#dfe3ea] bg-[#f8f9fb] px-3 py-1.5 text-xs text-[#374151]">
              {'· '.repeat(item.depth)}{item.name}
            </span>
          ))}
        </div>
      </section>

      {message && <div className="mb-4 border border-green-100 bg-green-50 px-4 py-3 text-sm text-green-700">{message}</div>}
      {error && <div className="mb-4 border border-red-100 bg-red-50 px-4 py-3 text-sm text-red-600">{error}</div>}

      {loading ? <div className="py-12 text-center text-sm text-[#bbb]">카테고리를 불러오는 중...</div> : (
        <DataTable<ApiCategoryNode>
          keyField="id"
          data={categories}
          emptyMessage="카테고리가 없습니다."
          columns={[
            { key: 'name', header: '메뉴명', render: (row) => <span className="font-medium">{'-'.repeat(row.depth)} {row.name}</span> },
            { key: 'active', header: '카테고리 상태', render: (row) => row.active ? '활성' : <span className="text-[#aaa]">비활성</span> },
            {
              key: 'visibleInNav', header: '상단 노출', render: (row) => (
                <button type="button" disabled={!row.active || savingId === row.id} onClick={() => save(row, !row.visibleInNav)} className={`min-w-16 border px-2 py-1 text-xs ${row.visibleInNav ? 'border-green-200 bg-green-50 text-green-700' : 'border-[#ddd] bg-white text-[#888]'} disabled:opacity-40`}>
                  {row.visibleInNav ? '노출' : '숨김'}
                </button>
              ),
            },
            {
              key: 'sortOrder', header: '정렬 순서', render: (row) => (
                <input type="number" value={drafts[row.id]?.sortOrder ?? row.sortOrder} onChange={(e) => setDrafts((prev) => ({ ...prev, [row.id]: { ...(prev[row.id] ?? { slug: row.slug ?? '' }), sortOrder: e.target.value } }))} className="w-20 border border-[#dfe3ea] px-2 py-1.5 text-sm" />
              ),
            },
            {
              key: 'slug', header: 'URL 슬러그', render: (row) => (
                <input value={drafts[row.id]?.slug ?? row.slug ?? ''} onChange={(e) => setDrafts((prev) => ({ ...prev, [row.id]: { ...(prev[row.id] ?? { sortOrder: String(row.sortOrder) }), slug: e.target.value } }))} placeholder="선택 입력" className="w-36 border border-[#dfe3ea] px-2 py-1.5 text-sm" />
              ),
            },
            { key: 'save', header: '관리', render: (row) => <Button variant="outline" size="sm" disabled={savingId === row.id} onClick={() => save(row)}>{savingId === row.id ? '저장 중' : '저장'}</Button> },
          ]}
        />
      )}
    </AdminLayout>
  );
}
