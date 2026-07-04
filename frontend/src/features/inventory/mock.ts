import type { InventoryItem, InventoryLog } from './types';

export const mockInventory: InventoryItem[] = [
  { id: 1, productId: 1, productName: '플리츠 플로럴 미디 원피스', categoryName: '원피스', stockQuantity: 38, safetyStock: 10, status: 'NORMAL', lastUpdatedAt: '2026-06-14T10:00:00' },
  { id: 2, productId: 2, productName: '린넨 퍼프슬리브 블라우스', categoryName: '블라우스', stockQuantity: 52, safetyStock: 10, status: 'NORMAL', lastUpdatedAt: '2026-06-14T10:00:00' },
  { id: 3, productId: 3, productName: '크롭 트위드 재킷', categoryName: '아우터', stockQuantity: 15, safetyStock: 10, status: 'NORMAL', lastUpdatedAt: '2026-06-14T10:00:00' },
  { id: 4, productId: 4, productName: '벌키 케이블 니트 베스트', categoryName: '니트', stockQuantity: 27, safetyStock: 10, status: 'NORMAL', lastUpdatedAt: '2026-06-14T10:00:00' },
  { id: 5, productId: 5, productName: '오버핏 스트라이프 티셔츠', categoryName: '티셔츠', stockQuantity: 80, safetyStock: 20, status: 'NORMAL', lastUpdatedAt: '2026-06-14T10:00:00' },
  { id: 6, productId: 6, productName: '새틴 랩 미니 스커트', categoryName: '스커트', stockQuantity: 3, safetyStock: 10, status: 'LOW', lastUpdatedAt: '2026-06-13T15:30:00' },
  { id: 7, productId: 7, productName: '와이드 슬랙스 팬츠', categoryName: '팬츠', stockQuantity: 44, safetyStock: 15, status: 'NORMAL', lastUpdatedAt: '2026-06-14T10:00:00' },
  { id: 8, productId: 8, productName: '셔링 홀터넥 원피스', categoryName: '원피스', stockQuantity: 19, safetyStock: 10, status: 'NORMAL', lastUpdatedAt: '2026-06-14T10:00:00' },
  { id: 9, productId: 9, productName: '시스루 볼륨 블라우스', categoryName: '블라우스', stockQuantity: 0, safetyStock: 10, status: 'OUT_OF_STOCK', lastUpdatedAt: '2026-06-12T09:00:00' },
  { id: 10, productId: 10, productName: '로브형 롱 코트', categoryName: '아우터', stockQuantity: 9, safetyStock: 10, status: 'LOW', lastUpdatedAt: '2026-06-13T12:00:00' },
];

export const mockInventoryLogs: InventoryLog[] = [
  { id: 1, productId: 1, productName: '플리츠 플로럴 미디 원피스', type: 'PURCHASE', quantity: 50, note: '초도 발주', createdAt: '2026-05-20T09:00:00' },
  { id: 2, productId: 6, productName: '새틴 랩 미니 스커트', type: 'ADJUSTMENT', quantity: -5, note: '불량 처리', createdAt: '2026-06-10T14:00:00' },
  { id: 3, productId: 9, productName: '시스루 볼륨 블라우스', type: 'PURCHASE', quantity: 30, note: '추가 발주', createdAt: '2026-06-01T10:00:00' },
  { id: 4, productId: 3, productName: '크롭 트위드 재킷', type: 'RETURN', quantity: 2, note: '고객 반품', createdAt: '2026-06-12T11:00:00' },
];
