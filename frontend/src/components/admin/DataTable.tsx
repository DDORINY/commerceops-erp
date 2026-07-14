import { ReactNode } from 'react';

interface Column<T> {
  key: string;
  header: string;
  render?: (row: T) => ReactNode;
  className?: string;
}

interface DataTableProps<T> {
  columns: Column<T>[];
  data: T[];
  keyField: keyof T;
  emptyMessage?: string;
}

export default function DataTable<T extends object>({
  columns,
  data,
  keyField,
  emptyMessage = '데이터가 없습니다.',
}: DataTableProps<T>) {
  return (
    <div className="max-w-full overflow-x-auto overscroll-x-contain border border-[#e8eaf0] bg-white" role="region" aria-label="데이터 표" tabIndex={0}>
      <table className="w-full min-w-max text-sm">
        <thead>
          <tr className="bg-[#f8f9fb] border-b border-[#e8eaf0]">
            {columns.map((col) => (
              <th
                key={col.key}
                className={[
                  'px-3 sm:px-4 py-3 text-left text-xs font-semibold text-[#8a9bb5] tracking-wide uppercase whitespace-nowrap',
                  col.className || '',
                ].join(' ')}
              >
                {col.header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {data.length === 0 ? (
            <tr>
              <td
                colSpan={columns.length}
                className="px-4 py-12 text-center text-[#bbb] text-sm"
              >
                {emptyMessage}
              </td>
            </tr>
          ) : (
            data.map((row) => (
              <tr
                key={String(row[keyField])}
                className="border-b border-[#f0f1f5] hover:bg-[#fafbfc] transition-colors"
              >
                {columns.map((col) => (
                  <td
                    key={col.key}
                    className={[
                      'px-3 sm:px-4 py-3.5 text-[#333] whitespace-nowrap',
                      col.className || '',
                    ].join(' ')}
                  >
                    {col.render
                      ? col.render(row)
                      : String((row as Record<string, unknown>)[col.key] ?? '-')}
                  </td>
                ))}
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
}
