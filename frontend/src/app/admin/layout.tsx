import type { Metadata } from 'next';

export const metadata: Metadata = {
  title: 'CommerceOps ERP | 관리자',
  description: 'CommerceOps ERP 관리자 화면',
};

export default function AdminRootLayout({ children }: { children: React.ReactNode }) {
  return <>{children}</>;
}
