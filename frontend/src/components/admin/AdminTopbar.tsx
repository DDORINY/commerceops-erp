export default function AdminTopbar({ title }: { title: string }) {
  return (
    <header className="h-[60px] bg-white border-b border-[#e8eaf0] flex items-center justify-between px-6 flex-shrink-0">
      <h1 className="text-base font-semibold text-[#1a1f2e]">{title}</h1>
      <div className="flex items-center gap-4">
        {/* 알림 */}
        <button className="relative text-[#8a9bb5] hover:text-[#1a1f2e] transition-colors">
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.8} d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
          </svg>
          <span className="absolute -top-0.5 -right-0.5 w-2 h-2 bg-[#f3a6b8] rounded-full" />
        </button>

        {/* 관리자 프로필 */}
        <div className="flex items-center gap-2.5">
          <div className="w-8 h-8 bg-[#1a1f2e] rounded-full flex items-center justify-center">
            <span className="text-white text-xs font-medium">관</span>
          </div>
          <div className="hidden md:block">
            <p className="text-xs font-medium text-[#1a1f2e]">관리자</p>
            <p className="text-[10px] text-[#8a9bb5]">admin@commerceops.com</p>
          </div>
        </div>
      </div>
    </header>
  );
}
