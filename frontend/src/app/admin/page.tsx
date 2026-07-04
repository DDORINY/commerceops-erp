'use client';

import { useState, useEffect } from 'react';
import AdminLayout from '@/components/admin/AdminLayout';
import StatCard from '@/components/admin/StatCard';
import DataTable from '@/components/admin/DataTable';
import Button from '@/components/common/Button';
import { adminService, type ApiDashboardSummary, type ApiSalesData, type ApiLowStockProduct } from '@/lib/services/adminService';
import { orderService, type ApiAdminOrder } from '@/lib/services/orderService';
import { formatPrice, formatDateTime, ORDER_STATUS_LABEL, ORDER_STATUS_COLOR } from '@/lib/format';

const statIcon = (label: string, color: string) => (
  <span className={`text-xs font-bold ${color}`}>{label}</span>
);

const PAYMENT_STATUS_LABEL: Record<string, string> = {
  READY: '결제 대기',
  PAID: '결제 완료',
  FAILED: '결제 실패',
  CANCELLED: '결제 취소',
  REFUNDED: '환불 완료',
};

const PAYMENT_STATUS_COLOR: Record<string, string> = {
  READY: 'bg-gray-100 text-gray-600',
  PAID: 'bg-green-50 text-green-700',
  FAILED: 'bg-red-100 text-red-700',
  CANCELLED: 'bg-gray-100 text-gray-500',
  REFUNDED: 'bg-amber-50 text-amber-700',
};

export default function AdminDashboardPage() {
  const [summary, setSummary] = useState<ApiDashboardSummary | null>(null);
  const [recentOrders, setRecentOrders] = useState<ApiAdminOrder[]>([]);
  const [lowStockProducts, setLowStockProducts] = useState<ApiLowStockProduct[]>([]);
  const [monthlySales, setMonthlySales] = useState<ApiSalesData[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    let mounted = true;

    const loadDashboard = async () => {
      setLoading(true);
      setError('');

      try {
        const [summaryData, orderPage, lowStockData, salesData] = await Promise.all([
          adminService.getDashboardSummary(),
          orderService.getAdminOrders(undefined, undefined, 0, 5),
          adminService.getLowStockProducts(5),
          adminService.getSales('MONTHLY'),
        ]);

        if (!mounted) return;
        setSummary(summaryData);
        setRecentOrders(orderPage.content);
        setLowStockProducts(lowStockData);
        setMonthlySales(salesData);
      } catch (err) {
        if (!mounted) return;
        setError(err instanceof Error ? err.message : '대시보드 통계를 불러오지 못했습니다.');
      } finally {
        if (mounted) setLoading(false);
      }
    };

    loadDashboard();

    return () => {
      mounted = false;
    };
  }, [reloadKey]);

  const maxSales = monthlySales.length > 0
    ? Math.max(...monthlySales.map((m) => m.salesAmount))
    : 1;
  const orderStatusCounts = summary?.orderStatusCounts ?? {};

  return (
    <AdminLayout title="대시보드">
      {loading ? (
        <div className="py-12 text-center text-[#bbb] text-sm">대시보드 통계를 불러오는 중...</div>
      ) : error ? (
        <div className="bg-white border border-[#f1c7c7] px-5 py-12 text-center">
          <p className="text-sm text-[#c43a3a]">{error}</p>
          <Button variant="outline" size="sm" onClick={() => setReloadKey((prev) => prev + 1)} className="mt-4">
            다시 불러오기
          </Button>
        </div>
      ) : (
      <>
      <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4 mb-6">
        <StatCard
          title="총 매출"
          value={formatPrice(summary?.totalSales ?? 0)}
          subtitle="취소/환불 제외"
          iconBgColor="bg-blue-100"
          icon={statIcon('매출', 'text-blue-600')}
        />
        <StatCard
          title="오늘 매출"
          value={formatPrice(summary?.todaySales ?? 0)}
          subtitle="오늘 결제 완료"
          iconBgColor="bg-green-100"
          icon={statIcon('오늘', 'text-green-700')}
        />
        <StatCard
          title="총 주문 수"
          value={`${summary?.totalOrders ?? 0}건`}
          subtitle="전체 누적"
          iconBgColor="bg-purple-100"
          icon={statIcon('주문', 'text-purple-600')}
        />
        <StatCard
          title="오늘 주문 수"
          value={`${summary?.todayOrders ?? 0}건`}
          subtitle="오늘 접수"
          iconBgColor="bg-yellow-100"
          icon={statIcon('신규', 'text-yellow-700')}
        />
        <StatCard
          title="전체 고객 수"
          value={`${summary?.totalCustomers ?? 0}명`}
          subtitle="가입 회원"
          iconBgColor="bg-sky-100"
          icon={statIcon('고객', 'text-sky-700')}
        />
        <StatCard
          title="전체 상품 수"
          value={`${summary?.totalProducts ?? 0}개`}
          subtitle="삭제 상품 제외"
          iconBgColor="bg-indigo-100"
          icon={statIcon('상품', 'text-indigo-700')}
        />
        <StatCard
          title="품절 상품"
          value={`${summary?.soldOutProductCount ?? 0}개`}
          subtitle="판매 불가"
          iconBgColor="bg-gray-100"
          icon={statIcon('품절', 'text-gray-600')}
        />
        <StatCard
          title="재고 부족 상품"
          value={`${summary?.lowStockProductCount ?? 0}개`}
          subtitle="즉시 확인 필요"
          iconBgColor="bg-red-100"
          icon={statIcon('재고', 'text-red-600')}
        />
      </div>

      <div className="bg-white border border-[#e8eaf0] p-5 mb-6">
        <h2 className="text-sm font-bold text-[#1a1f2e] mb-4">주문 상태별 현황</h2>
        <div className="grid grid-cols-2 sm:grid-cols-3 xl:grid-cols-7 gap-3">
          {Object.entries(ORDER_STATUS_LABEL).map(([status, label]) => (
            <div key={status} className="border border-[#f0f1f5] px-3 py-3">
              <p className="text-xs text-[#8a9bb5]">{label}</p>
              <p className="text-lg font-bold text-[#1a1f2e] mt-1">
                {orderStatusCounts[status] ?? 0}건
              </p>
            </div>
          ))}
        </div>
      </div>

      <div className="grid grid-cols-1 xl:grid-cols-3 gap-6">
        <div className="xl:col-span-2">
          <div className="bg-white border border-[#e8eaf0] p-5">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-sm font-bold text-[#1a1f2e]">최근 주문</h2>
              <a href="/admin/orders" className="text-xs text-blue-500 hover:text-blue-700 transition-colors">
                전체보기
              </a>
            </div>
            <DataTable<ApiAdminOrder>
              keyField="orderId"
              data={recentOrders}
              emptyMessage="최근 주문이 없습니다."
              columns={[
                { key: 'orderId', header: 'ID' },
                { key: 'orderNumber', header: '주문번호' },
                {
                  key: 'userName',
                  header: '주문자',
                  render: (row) => (
                    <div>
                      <p className="font-medium text-[#222]">{row.userName}</p>
                      <p className="text-xs text-[#999]">{row.userEmail}</p>
                    </div>
                  ),
                },
                { key: 'itemCount', header: '상품 수', render: (row) => `${row.itemCount}개` },
                {
                  key: 'totalPrice',
                  header: '금액',
                  render: (row) => formatPrice(row.totalPrice),
                },
                {
                  key: 'status',
                  header: '상태',
                  render: (row) => (
                    <span className={`text-xs font-medium px-2 py-0.5 ${ORDER_STATUS_COLOR[row.status] ?? ''}`}>
                      {ORDER_STATUS_LABEL[row.status] ?? row.status}
                    </span>
                  ),
                },
                {
                  key: 'paymentStatus',
                  header: '결제 상태',
                  render: (row) => (
                    <span className={`text-xs font-medium px-2 py-0.5 ${PAYMENT_STATUS_COLOR[row.paymentStatus] ?? ''}`}>
                      {PAYMENT_STATUS_LABEL[row.paymentStatus] ?? row.paymentStatus}
                    </span>
                  ),
                },
                {
                  key: 'createdAt',
                  header: '주문일시',
                  render: (row) => formatDateTime(row.createdAt),
                },
              ]}
            />
          </div>
        </div>

        <div>
          <div className="bg-white border border-[#e8eaf0] p-5">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-sm font-bold text-[#1a1f2e]">재고 부족 상품</h2>
              <a href="/admin/inventory" className="text-xs text-blue-500 hover:text-blue-700 transition-colors">
                재고 관리
              </a>
            </div>
            <div className="space-y-3">
              {lowStockProducts.map((item) => (
                <div
                  key={item.productId}
                  className="flex items-center justify-between py-3 border-b border-[#f0f1f5] last:border-0"
                >
                  <div>
                    <p className="text-sm text-[#333] font-medium line-clamp-1">{item.productName}</p>
                    <p className="text-xs text-[#999] mt-0.5">기준재고: {item.lowStockThreshold}개</p>
                  </div>
                  <span
                    className={[
                      'text-sm font-bold',
                      item.stockQuantity === 0 ? 'text-red-500' : 'text-yellow-600',
                    ].join(' ')}
                  >
                    {item.stockQuantity}개
                  </span>
                </div>
              ))}
              {lowStockProducts.length === 0 && (
                <p className="text-sm text-[#bbb] text-center py-4">재고 부족 상품이 없습니다.</p>
              )}
            </div>
          </div>

          <div className="bg-white border border-[#e8eaf0] p-5 mt-4">
            <h2 className="text-sm font-bold text-[#1a1f2e] mb-4">월별 매출 추이</h2>
            <div className="space-y-2.5">
              {monthlySales.slice(-4).map((month) => {
                const percent = maxSales > 0 ? Math.round((month.salesAmount / maxSales) * 100) : 0;
                return (
                  <div key={month.date}>
                    <div className="flex items-center justify-between text-xs mb-1">
                      <span className="text-[#777]">{month.date}</span>
                      <span className="font-medium text-[#333]">{formatPrice(month.salesAmount)}</span>
                    </div>
                    <div className="h-1.5 bg-[#f0f1f5] rounded-full overflow-hidden">
                      <div
                        className="h-full bg-blue-400 rounded-full"
                        style={{ width: `${percent}%` }}
                      />
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        </div>
      </div>
      </>
      )}
    </AdminLayout>
  );
}
