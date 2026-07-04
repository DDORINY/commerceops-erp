'use client';

import { ButtonHTMLAttributes, ReactNode } from 'react';

type Variant = 'primary' | 'secondary' | 'outline' | 'ghost' | 'danger';
type Size = 'sm' | 'md' | 'lg';

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: Variant;
  size?: Size;
  fullWidth?: boolean;
  children: ReactNode;
}

const variantStyles: Record<Variant, string> = {
  primary: 'bg-[#222] text-white hover:bg-[#444] active:bg-[#111]',
  secondary: 'bg-[#f3a6b8] text-white hover:bg-[#e890a3] active:bg-[#d97b93]',
  outline: 'border border-[#222] text-[#222] hover:bg-[#f5f5f5]',
  ghost: 'text-[#555] hover:bg-[#f5f5f5]',
  danger: 'bg-[#d94f4f] text-white hover:bg-[#c43a3a]',
};

const sizeStyles: Record<Size, string> = {
  sm: 'text-xs px-3 py-1.5',
  md: 'text-sm px-5 py-2.5',
  lg: 'text-base px-8 py-3',
};

export default function Button({
  variant = 'primary',
  size = 'md',
  fullWidth = false,
  className = '',
  children,
  ...props
}: ButtonProps) {
  return (
    <button
      className={[
        'inline-flex items-center justify-center font-medium transition-colors duration-150 cursor-pointer disabled:opacity-40 disabled:cursor-not-allowed',
        variantStyles[variant],
        sizeStyles[size],
        fullWidth ? 'w-full' : '',
        className,
      ]
        .filter(Boolean)
        .join(' ')}
      {...props}
    >
      {children}
    </button>
  );
}
