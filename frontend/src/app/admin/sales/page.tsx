'use client';

import { useState, useEffect } from 'react';
import AdminLayout from '@/components/admin/AdminLayout';
import StatCard from '@/components/admin/StatCard';
import DataTable from '@/components/admin/DataTable';
import { adminService, type ApiDashboardSummary, type ApiSalesData } from '@/lib/services/adminService';
import { orderService, type ApiAdminOrder } from '@/lib/services/orderService';
import { formatPrice, formatDate } from '@/lib/format';

export default function AdminSalesPage() {
  const [summary, setSummary] = useState<ApiDashboardSummary | null>(null);
  const [monthlySales, setMonthlySales] = useState<ApiSalesData[]>([]);
  const [recentPaidOrders, setRecentPaidOrders] = useState<ApiAdminOrder[]>([]);

  useEffect(() => {
    adminService.getDashboardSummary().then(setSummary).catch(() => {});
    adminService.getSales('MONTHLY').then(setMonthlySales).catch(() => {});
    orderService.getAdminOrders('PAID', undefined, 0, 20)
      .then((res) => setRecentPaidOrders(res.content))
      .catch(() => {});
  }, []);

  const maxRevenue = monthlySales.length > 0
    ? Math.max(...monthlySales.map((m) => m.salesAmount))
    : 1;

  const totalSales = summary?.totalSales ?? 0;
  const totalOrders = summary?.totalOrders ?? 0;
  const avgOrderValue = totalOrders > 0 ? Math.round(totalSales / totalOrders) : 0;

  return (
    <AdminLayout title="매출 통계">
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-6">
        <StatCard
          title="총 매출"
          value={formatPrice(totalSales)}
          subtitle="전체 누적"
          iconBgColor="bg-blue-100"
          icon={
            <svg className="w-5 h-5 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.8} d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" />
            </svg>
          }
        />
        <StatCard
          title="총 주문 수"
          value={`${totalOrders}건`}
          subtitle="전체 누적"
          iconBgColor="bg-purple-100"
          icon={
            <svg className="w-5 h-5 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.8} d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z" />
            </svg>
          }
        />
        <StatCard
          title="평균 주문액"
          value={formatPrice(avgOrderValue)}
          subtitle="전체 평균"
          iconBgColor="bg-green-100"
          icon={
            <svg className="w-5 h-5 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.8} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
            </svg>
          }
        />
      </div>

      <div className="grid grid-cols-1 xl:grid-cols-2 gap-6">
        <div className="bg-white border border-[#e8eaf0] p-5">
          <h2 className="text-sm font-bold text-[#1a1f2e] mb-5">월별 매출 추이</h2>
          <div className="space-y-4">
            {monthlySales.map((month) => {
              const percent = maxRevenue > 0 ? Math.round((month.salesAmount / maxRevenue) * 100) : 0;
              return (
                <div key={month.date}>
                  <div className="flex items-center justify-between text-xs mb-1.5">
                    <span className="text-[#777] w-16">{month.date}</span>
                    <div className="flex-1 mx-3">
                      <div className="h-5 bg-[#f0f1f5] rounded-sm overflow-hidden">
                        <div
                          className="h-full bg-[#1a1f2e] rounded-sm transition-all duration-500"
                          style={{ width: `${percent}%` }}
                        />
                      </div>
                    </div>
                    <span className="font-medium text-[#333] text-right w-24">
                      {formatPrice(month.salesAmount)}
                    </span>
                  </div>
                  <div className="text-[10px] text-[#999] pl-16 pr-6 text-right">
                    주문 {month.orderCount}건
                  </div>
                </div>
              );
            })}
            {monthlySales.length === 0 && (
              <p className="text-sm text-[#bbb] text-center py-4">매출 데이터가 없습니다.</p>
            )}
          </div>
        </div>

        <div className="bg-white border border-[#e8eaf0] p-5">
          <h2 className="text-sm font-bold text-[#1a1f2e] mb-4">월별 통계</h2>
          <DataTable<ApiSalesData>
            keyField="date"
            data={[...monthlySales].reverse()}
            columns={[
              { key: 'date', header: '월' },
              {
                key: 'salesAmount',
                header: '매출',
                render: (row) => formatPrice(row.salesAmount),
              },
              { key: 'orderCount', header: '주문 수', render: (row) => `${row.orderCount}건` },
              {
                key: 'salesAmount',
                header: '평균 주문액',
                render: (row) =>
                  formatPrice(row.orderCount > 0 ? Math.round(row.salesAmount / row.orderCount) : 0),
              },
            ]}
          />
        </div>
      </div>

      <div className="bg-white border border-[#e8eaf0] p-5 mt-6">
        <h2 className="text-sm font-bold text-[#1a1f2e] mb-4">최근 결제 완료 주문</h2>
        <DataTable<ApiAdminOrder>
          keyField="orderId"
          data={recentPaidOrders}
          columns={[
            { key: 'orderNumber', header: '주문번호' },
            { key: 'userName', header: '고객명' },
            {
              key: 'totalPrice',
              header: '결제금액',
              render: (row) => formatPrice(row.totalPrice),
            },
            {
              key: 'status',
              header: '상태',
              render: (row) => row.status,
            },
            {
              key: 'createdAt',
              header: '주문일',
              render: (row) => formatDate(row.createdAt),
            },
          ]}
        />
      </div>
    </AdminLayout>
  );
}
