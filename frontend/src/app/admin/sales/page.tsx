'use client';

import { useCallback, useEffect, useMemo, useState } from 'react';
import AdminLayout from '@/components/admin/AdminLayout';
import StatCard from '@/components/admin/StatCard';
import DataTable from '@/components/admin/DataTable';
import {
  adminService,
  type ApiDashboardSummary,
  type ApiSalesData,
  type ApiSalesPeriod,
  type ApiTopProduct,
} from '@/lib/services/adminService';
import { orderService, type ApiAdminOrder } from '@/lib/services/orderService';
import { formatPrice, formatDate, ORDER_STATUS_LABEL } from '@/lib/format';

type SalesPeriod = ApiSalesPeriod;

const PERIOD_FILTERS: { value: SalesPeriod; label: string }[] = [
  { value: 'DAILY', label: '일별' },
  { value: 'MONTHLY', label: '월별' },
];

const toDateInput = (date: Date) => date.toISOString().slice(0, 10);

const defaultStartDate = () => {
  const date = new Date();
  date.setMonth(date.getMonth() - 5);
  date.setDate(1);
  return toDateInput(date);
};

const defaultEndDate = () => toDateInput(new Date());

export default function AdminSalesPage() {
  const [summary, setSummary] = useState<ApiDashboardSummary | null>(null);
  const [salesData, setSalesData] = useState<ApiSalesData[]>([]);
  const [topProducts, setTopProducts] = useState<ApiTopProduct[]>([]);
  const [recentPaidOrders, setRecentPaidOrders] = useState<ApiAdminOrder[]>([]);
  const [period, setPeriod] = useState<SalesPeriod>('MONTHLY');
  const [startDate, setStartDate] = useState(defaultStartDate);
  const [endDate, setEndDate] = useState(defaultEndDate);
  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const fetchSales = useCallback(() => {
    Promise.allSettled([
      adminService.getDashboardSummary(),
      adminService.getSales(period, startDate, endDate),
      adminService.getTopProducts(10),
      orderService.getAdminOrders('PAID', undefined, 0, 10),
    ])
      .then(([summaryResult, salesResult, productsResult, ordersResult]) => {
        const errors: string[] = [];

        if (summaryResult.status === 'fulfilled') {
          setSummary(summaryResult.value);
        } else {
          setSummary(null);
          errors.push('요약 지표');
        }

        if (salesResult.status === 'fulfilled') {
          setSalesData(salesResult.value);
        } else {
          setSalesData([]);
          errors.push('기간별 매출');
        }

        if (productsResult.status === 'fulfilled') {
          setTopProducts(productsResult.value);
        } else {
          setTopProducts([]);
          errors.push('인기 상품');
        }

        if (ordersResult.status === 'fulfilled') {
          setRecentPaidOrders(ordersResult.value.content);
        } else {
          setRecentPaidOrders([]);
          errors.push('최근 주문');
        }

        setErrorMessage(errors.length > 0 ? `${errors.join(', ')} 데이터를 불러오지 못했습니다.` : null);
      })
      .finally(() => setLoading(false));
  }, [endDate, period, startDate]);

  useEffect(() => {
    fetchSales();
  }, [fetchSales]);

  const handlePeriodChange = (nextPeriod: SalesPeriod) => {
    setPeriod(nextPeriod);
    setLoading(true);
    setErrorMessage(null);
  };

  const handleRefresh = () => {
    setLoading(true);
    setErrorMessage(null);
    fetchSales();
  };

  const handleStartDateChange = (value: string) => {
    setStartDate(value);
    setLoading(true);
    setErrorMessage(null);
  };

  const handleEndDateChange = (value: string) => {
    setEndDate(value);
    setLoading(true);
    setErrorMessage(null);
  };

  const maxSales = salesData.length > 0
    ? Math.max(...salesData.map((item) => item.salesAmount))
    : 1;

  const selectedSalesAmount = useMemo(
    () => salesData.reduce((sum, item) => sum + item.salesAmount, 0),
    [salesData]
  );
  const selectedOrderCount = useMemo(
    () => salesData.reduce((sum, item) => sum + item.orderCount, 0),
    [salesData]
  );
  const selectedAvgOrderValue = selectedOrderCount > 0
    ? Math.round(selectedSalesAmount / selectedOrderCount)
    : 0;

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

      <div className="flex flex-wrap items-end gap-3 mb-4">
        <div className="flex items-center gap-2">
          {PERIOD_FILTERS.map((filter) => (
            <button
              key={filter.value}
              onClick={() => handlePeriodChange(filter.value)}
              className={[
                'px-4 py-2 text-xs font-medium border transition-colors',
                period === filter.value
                  ? 'border-[#1a1f2e] bg-[#1a1f2e] text-white'
                  : 'border-[#e8eaf0] text-[#8a9bb5] hover:border-[#1a1f2e] hover:text-[#1a1f2e] bg-white',
              ].join(' ')}
            >
              {filter.label}
            </button>
          ))}
        </div>
        <label className="text-xs font-medium text-[#777]">
          시작일
          <input
            type="date"
            value={startDate}
            onChange={(event) => handleStartDateChange(event.target.value)}
            className="block mt-1 border border-[#e8eaf0] px-3 py-2 text-xs text-[#333] bg-white"
          />
        </label>
        <label className="text-xs font-medium text-[#777]">
          종료일
          <input
            type="date"
            value={endDate}
            onChange={(event) => handleEndDateChange(event.target.value)}
            className="block mt-1 border border-[#e8eaf0] px-3 py-2 text-xs text-[#333] bg-white"
          />
        </label>
        <button
          onClick={handleRefresh}
          className="px-4 py-2 text-xs font-medium border border-[#1a1f2e] bg-white text-[#1a1f2e] hover:bg-[#1a1f2e] hover:text-white transition-colors"
        >
          조회
        </button>
      </div>

      {errorMessage && (
        <div className="mb-4 border border-red-100 bg-red-50 px-4 py-3 text-sm text-red-600">
          {errorMessage}
        </div>
      )}

      {loading ? (
        <div className="py-12 text-center text-[#bbb] text-sm">매출 데이터를 불러오는 중...</div>
      ) : (
        <>
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-6">
            <StatCard
              title="조회 기간 매출"
              value={formatPrice(selectedSalesAmount)}
              subtitle={`${startDate} ~ ${endDate}`}
              iconBgColor="bg-blue-50"
              icon={<span className="text-sm font-bold text-blue-600">₩</span>}
            />
            <StatCard
              title="조회 기간 주문 수"
              value={`${selectedOrderCount}건`}
              subtitle={period === 'MONTHLY' ? '월별 집계' : '일별 집계'}
              iconBgColor="bg-purple-50"
              icon={<span className="text-sm font-bold text-purple-600">#</span>}
            />
            <StatCard
              title="조회 기간 평균 주문액"
              value={formatPrice(selectedAvgOrderValue)}
              subtitle="매출 / 주문 수"
              iconBgColor="bg-green-50"
              icon={<span className="text-xs font-bold text-green-600">AVG</span>}
            />
          </div>

          <div className="grid grid-cols-1 xl:grid-cols-2 gap-6">
            <div className="bg-white border border-[#e8eaf0] p-5">
              <h2 className="text-sm font-bold text-[#1a1f2e] mb-5">
                {period === 'MONTHLY' ? '월별' : '일별'} 매출 추이
              </h2>
              <div className="space-y-4">
                {salesData.map((item) => {
                  const percent = maxSales > 0 ? Math.round((item.salesAmount / maxSales) * 100) : 0;
                  return (
                    <div key={item.date}>
                      <div className="flex items-center justify-between text-xs mb-1.5">
                        <span className="text-[#777] w-20">{item.date}</span>
                        <div className="flex-1 mx-3">
                          <div className="h-5 bg-[#f0f1f5] rounded-sm overflow-hidden">
                            <div
                              className="h-full bg-[#1a1f2e] rounded-sm transition-all duration-500"
                              style={{ width: `${percent}%` }}
                            />
                          </div>
                        </div>
                        <span className="font-medium text-[#333] text-right w-24">
                          {formatPrice(item.salesAmount)}
                        </span>
                      </div>
                      <div className="text-[10px] text-[#999] pl-20 pr-6 text-right">
                        주문 {item.orderCount}건
                      </div>
                    </div>
                  );
                })}
                {salesData.length === 0 && (
                  <p className="text-sm text-[#bbb] text-center py-4">매출 데이터가 없습니다.</p>
                )}
              </div>
            </div>

            <div className="bg-white border border-[#e8eaf0] p-5">
              <h2 className="text-sm font-bold text-[#1a1f2e] mb-4">
                {period === 'MONTHLY' ? '월별' : '일별'} 통계
              </h2>
              <DataTable<ApiSalesData>
                keyField="date"
                data={[...salesData].reverse()}
                emptyMessage="매출 데이터가 없습니다."
                columns={[
                  { key: 'date', header: period === 'MONTHLY' ? '월' : '일자' },
                  {
                    key: 'salesAmount',
                    header: '매출',
                    render: (row) => formatPrice(row.salesAmount),
                  },
                  { key: 'orderCount', header: '주문 수', render: (row) => `${row.orderCount}건` },
                  {
                    key: 'avgOrderValue',
                    header: '평균 주문액',
                    render: (row) =>
                      formatPrice(row.orderCount > 0 ? Math.round(row.salesAmount / row.orderCount) : 0),
                  },
                ]}
              />
            </div>
          </div>

          <div className="grid grid-cols-1 xl:grid-cols-2 gap-6 mt-6">
            <div className="bg-white border border-[#e8eaf0] p-5">
              <h2 className="text-sm font-bold text-[#1a1f2e] mb-4">인기 상품 TOP 10</h2>
              <DataTable<ApiTopProduct>
                keyField="productId"
                data={topProducts}
                emptyMessage="상품별 판매 지표가 없습니다."
                columns={[
                  { key: 'productName', header: '상품명' },
                  { key: 'orderCount', header: '판매 수량', render: (row) => `${row.orderCount}개` },
                  {
                    key: 'salesAmount',
                    header: '매출',
                    render: (row) => formatPrice(row.salesAmount),
                  },
                ]}
              />
            </div>

            <div className="bg-white border border-[#e8eaf0] p-5">
              <h2 className="text-sm font-bold text-[#1a1f2e] mb-4">최근 결제 완료 주문</h2>
              <DataTable<ApiAdminOrder>
                keyField="orderId"
                data={recentPaidOrders}
                emptyMessage="최근 결제 완료 주문이 없습니다."
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
                    render: (row) => ORDER_STATUS_LABEL[row.status] ?? row.status,
                  },
                  {
                    key: 'createdAt',
                    header: '주문일',
                    render: (row) => formatDate(row.createdAt),
                  },
                ]}
              />
            </div>
          </div>
        </>
      )}
    </AdminLayout>
  );
}
