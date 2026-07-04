'use client';

import { useEffect, useState } from 'react';
import AdminLayout from '@/components/admin/AdminLayout';
import DataTable from '@/components/admin/DataTable';
import Button from '@/components/common/Button';
import { couponService, type ApiCoupon } from '@/lib/services/couponService';
import { formatDateTime } from '@/lib/format';

type DiscountType = 'FIXED' | 'PERCENT';

interface CouponFormState {
  code: string;
  discountType: DiscountType;
  discountValue: string;
  minOrderAmount: string;
  maxUsage: string;
  expiresAt: string;
}

const DISCOUNT_TYPE_LABEL: Record<DiscountType, string> = {
  FIXED: '정액 할인',
  PERCENT: '퍼센트 할인',
};

const initialForm: CouponFormState = {
  code: '',
  discountType: 'FIXED',
  discountValue: '',
  minOrderAmount: '',
  maxUsage: '',
  expiresAt: '',
};

function formatWon(value: number): string {
  return `${value.toLocaleString('ko-KR')}원`;
}

function getCouponStatus(coupon: ApiCoupon): { label: string; className: string } {
  if (!coupon.active) {
    return { label: '비활성', className: 'bg-gray-100 text-gray-500' };
  }

  if (new Date(coupon.expiresAt).getTime() < Date.now()) {
    return { label: '만료', className: 'bg-red-100 text-red-600' };
  }

  if (coupon.usedCount >= coupon.maxUsage) {
    return { label: '소진', className: 'bg-yellow-100 text-yellow-700' };
  }

  return { label: '활성', className: 'bg-green-100 text-green-700' };
}

export default function AdminCouponsPage() {
  const [coupons, setCoupons] = useState<ApiCoupon[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [reloadKey, setReloadKey] = useState(0);
  const [showForm, setShowForm] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [deactivatingId, setDeactivatingId] = useState<number | null>(null);
  const [form, setForm] = useState<CouponFormState>(initialForm);

  useEffect(() => {
    let mounted = true;

    const loadCoupons = async () => {
      setLoading(true);
      setError('');

      try {
        const items = await couponService.getAdminCoupons();
        if (!mounted) return;
        setCoupons(items);
      } catch (err) {
        if (!mounted) return;
        setCoupons([]);
        setError(err instanceof Error ? err.message : '쿠폰 목록을 불러오지 못했습니다.');
      } finally {
        if (mounted) setLoading(false);
      }
    };

    loadCoupons();

    return () => {
      mounted = false;
    };
  }, [reloadKey]);

  const handleSubmit = async () => {
    if (!form.code.trim() || !form.discountValue || !form.maxUsage || !form.expiresAt) {
      alert('필수 항목을 입력해주세요.');
      return;
    }

    setSubmitting(true);
    try {
      const created = await couponService.createCoupon({
        code: form.code.trim().toUpperCase(),
        discountType: form.discountType,
        discountValue: Number(form.discountValue),
        minOrderAmount: Number(form.minOrderAmount) || 0,
        maxUsage: Number(form.maxUsage),
        expiresAt: form.expiresAt,
      });
      setCoupons((prev) => [created, ...prev]);
      setForm(initialForm);
      setShowForm(false);
    } catch (err) {
      alert(err instanceof Error ? err.message : '쿠폰 등록에 실패했습니다.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDeactivate = async (couponId: number) => {
    if (!confirm('이 쿠폰을 비활성화하시겠습니까?')) return;

    setDeactivatingId(couponId);
    try {
      await couponService.deactivateCoupon(couponId);
      setCoupons((prev) => prev.map((coupon) => (
        coupon.id === couponId ? { ...coupon, active: false } : coupon
      )));
    } catch (err) {
      alert(err instanceof Error ? err.message : '쿠폰 비활성화에 실패했습니다.');
    } finally {
      setDeactivatingId(null);
    }
  };

  return (
    <AdminLayout title="쿠폰 관리">
      <div className="flex items-center justify-between mb-6">
        <p className="text-sm text-[#8a9bb5]">쿠폰을 등록하고 사용 상태를 관리합니다.</p>
        <Button variant={showForm ? 'outline' : 'primary'} size="sm" onClick={() => setShowForm((v) => !v)}>
          {showForm ? '취소' : '쿠폰 등록'}
        </Button>
      </div>

      {showForm && (
        <div className="bg-white border border-[#e8eaf0] p-5 mb-6">
          <h2 className="text-sm font-bold text-[#333] mb-4">새 쿠폰 등록</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-medium text-[#8a9bb5] mb-1">쿠폰 코드 *</label>
              <input
                type="text"
                value={form.code}
                onChange={(e) => setForm((prev) => ({ ...prev, code: e.target.value.toUpperCase() }))}
                placeholder="SUMMER2026"
                className="w-full border border-[#e0e0e0] px-3 py-2 text-sm outline-none focus:border-[#1a1f2e] uppercase"
              />
            </div>
            <div>
              <label className="block text-xs font-medium text-[#8a9bb5] mb-1">할인 유형 *</label>
              <select
                value={form.discountType}
                onChange={(e) => setForm((prev) => ({ ...prev, discountType: e.target.value as DiscountType }))}
                className="w-full border border-[#e0e0e0] px-3 py-2 text-sm outline-none focus:border-[#1a1f2e]"
              >
                <option value="FIXED">정액 할인</option>
                <option value="PERCENT">퍼센트 할인</option>
              </select>
            </div>
            <div>
              <label className="block text-xs font-medium text-[#8a9bb5] mb-1">
                할인 값 * {form.discountType === 'PERCENT' ? '(%)' : '(원)'}
              </label>
              <input
                type="number"
                value={form.discountValue}
                onChange={(e) => setForm((prev) => ({ ...prev, discountValue: e.target.value }))}
                min={1}
                max={form.discountType === 'PERCENT' ? 100 : undefined}
                className="w-full border border-[#e0e0e0] px-3 py-2 text-sm outline-none focus:border-[#1a1f2e]"
              />
            </div>
            <div>
              <label className="block text-xs font-medium text-[#8a9bb5] mb-1">최소 주문 금액 (원)</label>
              <input
                type="number"
                value={form.minOrderAmount}
                onChange={(e) => setForm((prev) => ({ ...prev, minOrderAmount: e.target.value }))}
                min={0}
                placeholder="0"
                className="w-full border border-[#e0e0e0] px-3 py-2 text-sm outline-none focus:border-[#1a1f2e]"
              />
            </div>
            <div>
              <label className="block text-xs font-medium text-[#8a9bb5] mb-1">최대 사용 횟수 *</label>
              <input
                type="number"
                value={form.maxUsage}
                onChange={(e) => setForm((prev) => ({ ...prev, maxUsage: e.target.value }))}
                min={1}
                className="w-full border border-[#e0e0e0] px-3 py-2 text-sm outline-none focus:border-[#1a1f2e]"
              />
            </div>
            <div>
              <label className="block text-xs font-medium text-[#8a9bb5] mb-1">만료일 *</label>
              <input
                type="datetime-local"
                value={form.expiresAt}
                onChange={(e) => setForm((prev) => ({ ...prev, expiresAt: e.target.value }))}
                className="w-full border border-[#e0e0e0] px-3 py-2 text-sm outline-none focus:border-[#1a1f2e]"
              />
            </div>
          </div>
          <div className="mt-4 flex justify-end">
            <Button variant="primary" size="sm" onClick={handleSubmit} disabled={submitting}>
              {submitting ? '등록 중...' : '등록'}
            </Button>
          </div>
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
        <DataTable<ApiCoupon>
          keyField="id"
          data={coupons}
          emptyMessage="등록된 쿠폰이 없습니다."
          columns={[
            {
              key: 'code',
              header: '코드',
              render: (coupon) => <span className="font-mono font-bold text-[#1a1f2e]">{coupon.code}</span>,
            },
            {
              key: 'discountType',
              header: '유형',
              render: (coupon) => DISCOUNT_TYPE_LABEL[coupon.discountType] ?? coupon.discountType,
            },
            {
              key: 'discountValue',
              header: '할인값',
              className: 'text-right',
              render: (coupon) => (
                coupon.discountType === 'PERCENT'
                  ? `${coupon.discountValue}%`
                  : formatWon(coupon.discountValue)
              ),
            },
            {
              key: 'minOrderAmount',
              header: '최소 주문',
              className: 'text-right',
              render: (coupon) => (coupon.minOrderAmount > 0 ? formatWon(coupon.minOrderAmount) : '-'),
            },
            {
              key: 'usedCount',
              header: '사용/최대',
              className: 'text-right tabular-nums',
              render: (coupon) => `${coupon.usedCount} / ${coupon.maxUsage}`,
            },
            {
              key: 'expiresAt',
              header: '만료일',
              render: (coupon) => <span className="text-xs text-[#555]">{formatDateTime(coupon.expiresAt)}</span>,
            },
            {
              key: 'active',
              header: '상태',
              className: 'text-center',
              render: (coupon) => {
                const status = getCouponStatus(coupon);
                return (
                  <span className={`inline-block px-2 py-0.5 text-xs font-medium ${status.className}`}>
                    {status.label}
                  </span>
                );
              },
            },
            {
              key: 'actions',
              header: '',
              className: 'text-right',
              render: (coupon) => (
                coupon.active ? (
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => handleDeactivate(coupon.id)}
                    disabled={deactivatingId === coupon.id}
                    className="text-[#d94f4f] hover:text-[#c43a3a]"
                  >
                    {deactivatingId === coupon.id ? '처리 중...' : '비활성화'}
                  </Button>
                ) : (
                  <span className="text-xs text-[#bbb]">-</span>
                )
              ),
            },
          ]}
        />
      )}
    </AdminLayout>
  );
}
