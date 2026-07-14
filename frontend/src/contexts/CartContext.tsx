'use client';

import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { cartService } from '@/lib/services/cartService';
import { getAccessToken } from '@/lib/auth';

type CartContextValue = { totalQuantity: number; refreshCart: () => Promise<void>; clearCartBadge: () => void };
const CartContext = createContext<CartContextValue | null>(null);

export function CartProvider({ children }: { children: React.ReactNode }) {
  const [totalQuantity, setTotalQuantity] = useState(0);
  const refreshCart = useCallback(async () => {
    if (!getAccessToken()) { setTotalQuantity(0); return; }
    try { setTotalQuantity((await cartService.getCart()).totalQuantity); } catch { setTotalQuantity(0); }
  }, []);
  const clearCartBadge = useCallback(() => setTotalQuantity(0), []);
  useEffect(() => {
    const initialRefresh = window.setTimeout(() => void refreshCart(), 0);
    const sync = () => void refreshCart();
    window.addEventListener('auth-changed', sync);
    window.addEventListener('cart-changed', sync);
    window.addEventListener('storage', sync);
    return () => {
      window.clearTimeout(initialRefresh);
      window.removeEventListener('auth-changed', sync);
      window.removeEventListener('cart-changed', sync);
      window.removeEventListener('storage', sync);
    };
  }, [refreshCart]);
  const value = useMemo(() => ({ totalQuantity, refreshCart, clearCartBadge }), [totalQuantity, refreshCart, clearCartBadge]);
  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
}
export function useCart() { const value = useContext(CartContext); if (!value) throw new Error('useCart must be used within CartProvider'); return value; }
export function notifyCartChanged() { if (typeof window !== 'undefined') window.dispatchEvent(new Event('cart-changed')); }
