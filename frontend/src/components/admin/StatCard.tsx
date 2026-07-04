import { ReactNode } from 'react';

interface StatCardProps {
  title: string;
  value: string | number;
  subtitle?: string;
  trend?: number;
  icon: ReactNode;
  iconBgColor?: string;
}

export default function StatCard({
  title,
  value,
  subtitle,
  trend,
  icon,
  iconBgColor = 'bg-blue-100',
}: StatCardProps) {
  const trendPositive = trend !== undefined && trend >= 0;

  return (
    <div className="bg-white border border-[#e8eaf0] p-5 rounded-sm">
      <div className="flex items-start justify-between mb-4">
        <div className={`w-11 h-11 ${iconBgColor} rounded-sm flex items-center justify-center`}>
          {icon}
        </div>
        {trend !== undefined && (
          <span
            className={[
              'text-xs font-medium px-2 py-0.5',
              trendPositive
                ? 'bg-green-50 text-green-600'
                : 'bg-red-50 text-red-500',
            ].join(' ')}
          >
            {trendPositive ? '+' : ''}
            {trend}%
          </span>
        )}
      </div>
      <p className="text-2xl font-bold text-[#1a1f2e] mb-1">{value}</p>
      <p className="text-sm font-medium text-[#555]">{title}</p>
      {subtitle && <p className="text-xs text-[#999] mt-0.5">{subtitle}</p>}
    </div>
  );
}
