import type { DashboardStats, MonthlySales, RecentOrder, LowStockProduct } from './types';

export const mockDashboardStats: DashboardStats = {
  totalRevenue: 12480000,
  totalOrders: 248,
  lowStockCount: 3,
  newOrders: 12,
  revenueGrowthRate: 8.4,
  orderGrowthRate: 5.2,
};

export const mockMonthlySales: MonthlySales[] = [
  { month: '2026-01', revenue: 8200000, orderCount: 164 },
  { month: '2026-02', revenue: 9100000, orderCount: 182 },
  { month: '2026-03', revenue: 10300000, orderCount: 206 },
  { month: '2026-04', revenue: 9800000, orderCount: 196 },
  { month: '2026-05', revenue: 11500000, orderCount: 230 },
  { month: '2026-06', revenue: 12480000, orderCount: 248 },
];

export const mockRecentOrders: RecentOrder[] = [
  { orderId: 5, orderNumber: 'ORD-20260610-0005', customerName: '정다현', totalPrice: 90000, status: 'CANCELLED', createdAt: '2026-06-10T09:12:00' },
  { orderId: 4, orderNumber: 'ORD-20260608-0004', customerName: '최민서', totalPrice: 135000, status: 'COMPLETED', createdAt: '2026-06-08T16:30:00' },
  { orderId: 3, orderNumber: 'ORD-20260605-0003', customerName: '이혜진', totalPrice: 38000, status: 'PENDING', createdAt: '2026-06-05T08:45:00' },
  { orderId: 2, orderNumber: 'ORD-20260603-0002', customerName: '박수연', totalPrice: 185000, status: 'PAID', createdAt: '2026-06-03T14:10:00' },
  { orderId: 1, orderNumber: 'ORD-20260601-0001', customerName: '김지은', totalPrice: 55000, status: 'SHIPPING', createdAt: '2026-06-01T10:23:00' },
];

export const mockLowStockProducts: LowStockProduct[] = [
  { productId: 9, productName: '시스루 볼륨 블라우스', stockQuantity: 0, safetyStock: 10 },
  { productId: 6, productName: '새틴 랩 미니 스커트', stockQuantity: 3, safetyStock: 10 },
  { productId: 10, productName: '로브형 롱 코트', stockQuantity: 9, safetyStock: 10 },
];
