import Link from "next/link";
import { searchChannels } from "@/lib/api/endpoints/channel/channel";
import { ModeToggle } from "@/components/common/mode-toggle";
import { SearchBar, MobileSearchButton } from "./search-bar";
import { UserMenu } from "./user-menu";
import { MobileSidebar } from "./mobile-sidebar";
import { SidebarProvider } from "./sidebar-context";
import { SidebarToggle } from "./sidebar-toggle";
import { PcSidebar } from "./pc-sidebar";
import { MainContent } from "./main-content";
import { getServerRequestOptions } from "@/lib/api/server-request-options";

export const dynamic = "force-dynamic";

export default async function AppLayout({
    children,
}: Readonly<{
    children: React.ReactNode;
}>) {
    // API 호출
    const requestOptions = await getServerRequestOptions();
    const data = await searchChannels(
        {
            request: {},
            pageable: {
                page: 0,
                size: 20,
            },
        },
        requestOptions
    );

    const channels = data?.content || [];


    return (
        <SidebarProvider>
            <div className={"bg-background"}>
                {/* 헤더 */}
                <header className="sticky top-0 left-0 right-0 h-14 bg-background z-50">
                    <div className="h-full px-4 flex items-center justify-between gap-4">
                        {/* 왼쪽: 로고. PC에서 pl-1로 햄버거 중심 X를 사이드바 아이콘 중심(40px)과 맞춘다. */}
                        <div className="flex-shrink-0 flex items-center gap-2 md:pl-1">
                            {/* PC 사이드바 토글 */}
                            <SidebarToggle />

                            {/* 모바일 사이드바 */}
                            <MobileSidebar channels={channels} />

                            <Link href="/" className="text-lg md:text-xl font-bold text-primary whitespace-nowrap">
                                StreamArchive
                            </Link>
                        </div>

                        {/* 중앙: 검색창 (PC 전용) */}
                        <SearchBar />

                        {/* 오른쪽: 검색(모바일) & 토글 & 유저 메뉴 */}
                        <div className="flex-shrink-0 flex items-center gap-1 md:gap-2">
                            {/* 검색 아이콘: 모바일 전용 */}
                            <MobileSearchButton />
                            {/* 테마 토글: PC만 노출 (모바일은 유저 메뉴 안으로 이동) */}
                            <div className="hidden md:block">
                                <ModeToggle />
                            </div>
                            <UserMenu />
                        </div>
                    </div>
                    {/*<Separator />*/}
                </header>

                {/* PC 사이드바 */}
                <PcSidebar channels={channels} />

                {/* 내용 */}
                <MainContent>{children}</MainContent>
            </div>
        </SidebarProvider>
    );
}
