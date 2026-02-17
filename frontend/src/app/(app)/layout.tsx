import Link from "next/link";
import { Menu } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Sheet, SheetContent, SheetTrigger } from "@/components/ui/sheet";
import { searchChannels } from "@/lib/api/endpoints/channel/channel";
import { ModeToggle } from "@/components/common/mode-toggle";
import { SearchBar } from "./search-bar";
import { UserMenu } from "./user-menu";
import { SidebarContent } from "./sidebar-content";

export const dynamic = "force-dynamic";

export default async function AppLayout({
    children,
}: Readonly<{
    children: React.ReactNode;
}>) {
    // API 호출
    const data = await searchChannels({
        request: {},
        pageable: {
            page: 0,
            size: 20,
        },
    });

    const channels = data?.content || [];


    return (
        <div className={"bg-background"}>
            {/* 헤더 */}
            <header className="sticky top-0 left-0 right-0 h-14 bg-background z-50">
                <div className="h-full px-4 flex items-center justify-between gap-4">
                    {/* 왼쪽: 로고 */}
                    <div className="flex-shrink-0 flex items-center gap-2">
                        {/* PC 사이드바 토글 */}
                        <Button variant="ghost" size="icon" className="hidden md:flex">
                            <Menu />
                        </Button>

                        {/* 모바일 사이드바 */}
                        <Sheet>
                            <SheetTrigger asChild>
                                <Button variant="ghost" size="icon" className="md:hidden">
                                    <Menu />
                                </Button>
                            </SheetTrigger>
                            <SheetContent side="left" className="w-64">
                                <SidebarContent channels={channels} isSheet />
                            </SheetContent>
                        </Sheet>

                        <Link href="/" className="text-xl font-bold text-primary">
                            <span className="md:hidden">S</span>
                            <span className="hidden md:inline">StreamArchive</span>
                        </Link>
                    </div>

                    {/* 중앙: 검색창 */}
                    <SearchBar />

                    {/* 오른쪽: 토글 & 유저 메뉴 */}
                    <div className="flex-shrink-0 flex items-center gap-2">
                        <ModeToggle />
                        <UserMenu />
                    </div>
                </div>
                {/*<Separator />*/}
            </header>

            {/* PC 사이드바 */}
            <div className="hidden md:block fixed w-60 h-full">
                {/*<Separator orientation="vertical" className="absolute right-0"></Separator>*/}
                <SidebarContent channels={channels} />
            </div>

            {/* 내용 */}
            <div className="md:ml-60">
                {children}
            </div>
        </div>
    );
}