import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "CommerceOps | 패션 쇼핑몰",
  description: "CommerceOps 여성 의류 쇼핑몰",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ko" className="h-full">
      <body className="min-h-full flex flex-col bg-white text-[#222] antialiased">
        {children}
      </body>
    </html>
  );
}
