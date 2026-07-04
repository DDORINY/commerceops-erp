export interface DashboardStats {
  totalRevenue: number;
  totalOrders: number;
  lowStockCount: number;
  newOrders: number;
  revenueGrowthRate: number;
  orderGrowthRate: number;
}

export interface MonthlySales {
  month: string;
  revenue: number;
  orderCount: number;
}

export interface DailySales {
  date: string;
  revenue: number;
  orderCount: number;
}

export interface RecentOrder {
  orderId: number;
  orderNumber: string;
  customerName: string;
  totalPrice: number;
  status: string;
  createdAt: string;
}

export interface LowStockProduct {
  productId: number;
  productName: string;
  stockQuantity: number;
  safetyStock: number;
}
