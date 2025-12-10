import Link from "next/link";
import { Clock, HomeIcon, Library, Menu, ThumbsUp } from "lucide-react";
import { Separator } from "@/components/ui/separator";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import { Sheet, SheetContent, SheetTrigger } from "@/components/ui/sheet";
import { searchChannels } from "@/lib/api/endpoints/channel/channel";
import { ModeToggle } from "@/components/common/mode-toggle";
import { SearchBar } from "./search-bar";
import { UserMenu } from "./user-menu";

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

    const SidebarContent = () => (
        <div className="flex flex-col h-full py-4">
            {/* 메인 메뉴 */}
            <div className="px-3 mb-4">
                <Link
                    href="/public"
                    className="flex items-center gap-4 px-3 py-2 rounded-lg hover:bg-secondary transition"
                >
                    <HomeIcon className="w-5 h-5" />
                    <span>홈</span>
                </Link>

                <Link
                    href="/public"
                    className="flex items-center gap-4 px-3 py-2 rounded-lg hover:bg-secondary transition"
                >
                    <Library className="w-5 h-5" />
                    <span>보관함</span>
                </Link>

                <Link
                    href="/my/history"
                    className="flex items-center gap-4 px-3 py-2 rounded-lg hover:bg-secondary transition"
                >
                    <Clock className="w-5 h-5" />
                    <span>시청 기록</span>
                </Link>

                <Link
                    href="/public"
                    className="flex items-center gap-4 px-3 py-2 rounded-lg hover:bg-secondary transition"
                >
                    <ThumbsUp className="w-5 h-5" />
                    <span>좋아요</span>
                </Link>
            </div>

            <Separator className="mb-4" />

            {/* 채널 목록 */}
            <div className="px-3 overflow-y-auto">
                <h3 className="px-3 mb-2 text-sm font-semibold text-muted-foreground">채널</h3>
                {channels.map((channel) => (
                    <Link
                        key={channel.uuid}
                        href={"/channels/" + channel.uuid}
                        className="flex items-center gap-4 px-3 py-2 rounded-lg hover:bg-secondary transition"
                    >
                        <Avatar className="w-8 h-8">
                            <AvatarImage src={channel.profileUrl} />
                            <AvatarFallback>{channel.name[0]}</AvatarFallback>
                        </Avatar>
                        <span>{channel.name}</span>
                    </Link>
                ))}
            </div>
        </div>
    );

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
                                <SidebarContent />
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
                <SidebarContent />
            </div>

            {/* 내용 */}
            <div className="md:ml-60">
                {children}
            </div>
        </div>
    );
}