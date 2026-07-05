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
      setError(err instanceof Error ? err.message : 'Failed to load categories.');
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
      setMessage('Category name is required.');
      return;
    }

    setSaving(true);
    setMessage('');
    try {
      const payload = buildPayload();
      if (editingId) {
        await categoryService.updateCategory(editingId, payload);
        setMessage('Category updated.');
      } else {
        await categoryService.createCategory(payload);
        setMessage('Category created.');
      }
      resetForm();
      await loadCategories();
    } catch (err) {
      setMessage(err instanceof Error ? err.message : 'Failed to save category.');
    } finally {
      setSaving(false);
    }
  };

  const parentOptions = categories.filter((category) => category.id !== editingId);

  return (
    <AdminLayout title="Category Management">
      <div className="grid grid-cols-1 xl:grid-cols-[360px_1fr] gap-5">
        <form onSubmit={handleSubmit} className="bg-white border border-[#e8eaf0] p-5 space-y-4">
          <div>
            <h2 className="text-sm font-bold text-[#1a1f2e]">
              {editingId ? 'Edit Category' : 'Create Category'}
            </h2>
            <p className="mt-1 text-xs text-[#8a9bb5]">
              Active and nav-visible categories appear in the shop header.
            </p>
          </div>

          {message && (
            <p className="border border-[#e8eaf0] bg-[#f7f8fc] px-3 py-2 text-xs text-[#4f5b70]">
              {message}
            </p>
          )}

          <Input
            label="Name"
            value={form.name}
            onChange={(e) => set('name', e.target.value)}
            fullWidth
          />

          <div>
            <label className="block text-sm font-medium text-[#444] mb-1">Parent</label>
            <select
              value={form.parentId}
              onChange={(e) => set('parentId', e.target.value)}
              className="border border-[#ddd] bg-white text-[#222] text-sm px-3 py-2.5 outline-none focus:border-[#222] w-full"
            >
              <option value="">Root category</option>
              {parentOptions.map((category) => (
                <option key={category.id} value={category.id}>
                  {'-'.repeat(category.depth)} {category.name}
                </option>
              ))}
            </select>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <Input
              label="Sort Order"
              type="number"
              value={form.sortOrder}
              onChange={(e) => set('sortOrder', e.target.value)}
              fullWidth
            />
            <Input
              label="Slug"
              value={form.slug}
              onChange={(e) => set('slug', e.target.value)}
              placeholder="optional"
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
              Active
            </label>
            <label className="flex items-center gap-2 text-sm text-[#555]">
              <input
                type="checkbox"
                checked={form.visibleInNav}
                onChange={(e) => set('visibleInNav', e.target.checked)}
                className="accent-[#222]"
              />
              Visible in shop navigation
            </label>
          </div>

          <div className="flex justify-end gap-2">
            {editingId && (
              <Button type="button" variant="outline" onClick={resetForm}>
                Cancel
              </Button>
            )}
            <Button type="submit" variant="primary" disabled={saving}>
              {saving ? 'Saving...' : editingId ? 'Update' : 'Create'}
            </Button>
          </div>
        </form>

        <div>
          {loading ? (
            <div className="py-12 text-center text-[#bbb] text-sm">Loading categories...</div>
          ) : error ? (
            <div className="bg-white border border-[#f1c7c7] px-5 py-12 text-center">
              <p className="text-sm text-[#c43a3a]">{error}</p>
              <Button variant="outline" size="sm" onClick={loadCategories} className="mt-4">
                Retry
              </Button>
            </div>
          ) : (
            <DataTable<ApiCategoryNode>
              keyField="id"
              data={categories}
              emptyMessage="No categories."
              columns={[
                {
                  key: 'name',
                  header: 'Name',
                  render: (row) => (
                    <span className="font-medium text-[#222]">
                      {'-'.repeat(row.depth)} {row.name}
                    </span>
                  ),
                },
                { key: 'parentId', header: 'Parent ID', render: (row) => row.parentId ?? '-' },
                { key: 'sortOrder', header: 'Sort' },
                {
                  key: 'active',
                  header: 'Active',
                  render: (row) => row.active ? 'Y' : 'N',
                },
                {
                  key: 'visibleInNav',
                  header: 'Nav',
                  render: (row) => row.visibleInNav ? 'Y' : 'N',
                },
                { key: 'slug', header: 'Slug', render: (row) => row.slug || '-' },
                {
                  key: 'actions',
                  header: 'Manage',
                  render: (row) => (
                    <Button variant="outline" size="sm" onClick={() => handleEdit(row)}>
                      Edit
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
