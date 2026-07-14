'use client';

import { useCallback, useEffect, useState } from 'react';
import Link from 'next/link';
import ShopHeader from '@/components/shop/ShopHeader';
import ShopFooter from '@/components/shop/ShopFooter';
import AddressCard from '@/components/address/AddressCard';
import AddressForm from '@/components/address/AddressForm';
import {
  addressService,
  type Address,
  type AddressInput,
} from '@/lib/services/addressService';

export default function AddressesPage() {
  const [items, setItems] = useState<Address[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [editing, setEditing] = useState<Address | null>(null);
  const [showForm, setShowForm] = useState(false);

  const load = useCallback(async () => {
    try {
      setItems(await addressService.list());
      setError('');
    } catch (loadError) {
      setError(loadError instanceof Error ? loadError.message : '배송지를 불러오지 못했습니다.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    const initialLoad = window.setTimeout(() => void load(), 0);
    return () => window.clearTimeout(initialLoad);
  }, [load]);

  const save = async (data: AddressInput) => {
    if (editing) await addressService.update(editing.addressId, data);
    else await addressService.create(data);
    setEditing(null);
    setShowForm(false);
    await load();
  };

  const remove = async (address: Address) => {
    if (!confirm(`'${address.addressName}' 배송지를 삭제하시겠습니까?`)) return;
    try {
      await addressService.remove(address.addressId);
      await load();
    } catch (removeError) {
      setError(removeError instanceof Error ? removeError.message : '삭제에 실패했습니다.');
    }
  };

  return (
    <>
      <ShopHeader />
      <main className="mx-auto max-w-[900px] px-4 py-10">
        <div className="mb-8 flex items-center justify-between border-b pb-4">
          <div>
            <Link href="/mypage" className="text-xs text-[#888]">← 마이페이지</Link>
            <h1 className="mt-2 text-xl font-bold">배송지 관리</h1>
          </div>
          <button
            onClick={() => { setEditing(null); setShowForm(true); }}
            className="bg-[#222] px-4 py-2 text-sm text-white"
          >
            신규 등록
          </button>
        </div>
        {error && <p className="mb-4 bg-red-50 p-3 text-sm text-red-600">{error}</p>}
        {showForm && (
          <div className="mb-6 border p-5">
            <AddressForm
              initial={editing ?? undefined}
              onSubmit={save}
              onCancel={() => { setShowForm(false); setEditing(null); }}
            />
          </div>
        )}
        {loading ? (
          <p className="py-16 text-center text-sm text-[#999]">불러오는 중...</p>
        ) : items.length === 0 ? (
          <p className="py-16 text-center text-sm text-[#999]">등록된 배송지가 없습니다.</p>
        ) : (
          <div className="grid gap-4 sm:grid-cols-2">
            {items.map((address) => (
              <AddressCard
                key={address.addressId}
                address={address}
                onEdit={() => { setEditing(address); setShowForm(true); }}
                onDelete={() => void remove(address)}
                onDefault={() => void addressService.setDefault(address.addressId)
                  .then(load)
                  .catch((defaultError) => setError(defaultError instanceof Error ? defaultError.message : '기본 배송지 설정에 실패했습니다.'))}
              />
            ))}
          </div>
        )}
      </main>
      <ShopFooter />
    </>
  );
}
