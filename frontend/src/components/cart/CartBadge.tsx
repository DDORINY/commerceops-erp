'use client';
import { useCart } from '@/contexts/CartContext';
export default function CartBadge() { const { totalQuantity } = useCart(); if (totalQuantity <= 0) return null; return <span className="absolute -right-2 -top-2 min-w-5 rounded-full bg-[#d94f4f] px-1 text-center text-[10px] font-bold leading-5 text-white">{totalQuantity > 99 ? '99+' : totalQuantity}</span>; }
