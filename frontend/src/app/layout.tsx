import type {Metadata} from "next";
import "./globals.css";
import {QueryProvider} from "@/components/providers/query-provider";

export const metadata: Metadata = {
  title: {
    default: "Stream Archive",
    template: "%s | Stream Archive",
  },
  description: "멀티 플랫폼 스트리밍 녹화 시스템",
  openGraph: {
    title: "Stream Archive",
    description: "멀티 플랫폼 스트리밍 녹화 시스템",
    type: "website",
    locale: "ko_KR",
  },
  twitter: {
    card: "summary_large_image",
    title: "Stream Archive",
    description: "멀티 플랫폼 스트리밍 녹화 시스템",
  },
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ko">
      <body className="antialiased">
        <QueryProvider>
          {children}
        </QueryProvider>
      </body>
    </html>
  );
}
