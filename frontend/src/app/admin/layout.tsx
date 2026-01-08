"use client";

import {
    Sidebar,
    SidebarContent,
    SidebarFooter,
    SidebarGroup,
    SidebarGroupContent,
    SidebarGroupLabel,
    SidebarHeader,
    SidebarMenu,
    SidebarMenuButton,
    SidebarMenuItem,
    SidebarProvider,
    SidebarTrigger,
    useSidebar
} from "@/components/ui/sidebar";
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from "@/components/ui/dropdown-menu";
import {
    Calendar,
    ChevronUp,
    LayoutDashboard,
    LogOut,
    LucideIcon,
    PlaySquare,
    Radio,
    Settings,
    Tv,
    Users,
    Video,
    User,
    Home
} from "lucide-react";
import Link from "next/link";
import {
    Breadcrumb,
    BreadcrumbItem,
    BreadcrumbList,
    BreadcrumbPage,
    BreadcrumbSeparator,
} from "@/components/ui/breadcrumb";
import { Separator } from "@/components/ui/separator";
import { usePathname, useRouter } from "next/navigation";
import { useQueryClient } from "@tanstack/react-query";
import { ModeToggle } from "@/components/common/mode-toggle";
import { useGetUserMe } from "@/lib/api/endpoints/user/user";
import { useLogout } from "@/lib/api/endpoints/auth/auth";

interface MenuItem {
    title: string;
    href: string;
    icon: LucideIcon;
}

interface MenuGroup {
    label: string;
    items: MenuItem[];
}

const menuConfig: MenuGroup[] = [
    {
        label: "대시보드",
        items: [
            { title: "대시보드", href: "/admin", icon: LayoutDashboard },
        ],
    },
    {
        label: "채널",
        items: [
            { title: "채널 관리", href: "/admin/channels", icon: Tv },
            { title: "채널 플랫폼 관리", href: "/admin/channel-platforms", icon: Radio },
        ],
    },
    {
        label: "녹화",
        items: [
            { title: "녹화 스케줄 관리", href: "/admin/schedules", icon: Calendar },
            { title: "녹화 관리", href: "/admin/records", icon: Video },
            { title: "동영상 관리", href: "/admin/videos", icon: PlaySquare },
        ],
    },
    {
        label: "사용자",
        items: [
            { title: "사용자 관리", href: "/admin/users", icon: Users },
        ],
    },
    {
        label: "시스템",
        items: [
            { title: "시스템 설정", href: "/admin/settings", icon: Settings },
        ],
    },
];

function SidebarMenuContent() {
    const { setOpenMobile } = useSidebar();
    const pathname = usePathname();

    // 현재 경로가 링크와 일치하는지 확인
    const isActive = (href: string) => {
        if (href === "/admin") {
            return pathname === "/admin";
        }
        return pathname === href || pathname.startsWith(href + "/");
    };

    return (
        <>
            {menuConfig.map((group) => (
                <SidebarGroup key={group.label}>
                    <SidebarGroupLabel>{group.label}</SidebarGroupLabel>
                    <SidebarGroupContent>
                        <SidebarMenu>
                            {group.items.map((item) => (
                                <SidebarMenuItem key={item.href}>
                                    <SidebarMenuButton asChild isActive={isActive(item.href)}>
                                        <Link href={item.href} onClick={() => setOpenMobile(false)}>
                                            <item.icon />
                                            <span>{item.title}</span>
                                        </Link>
                                    </SidebarMenuButton>
                                </SidebarMenuItem>
                            ))}
                        </SidebarMenu>
                    </SidebarGroupContent>
                </SidebarGroup>
            ))}
        </>
    );
}

export default function AdminLayout({
    children,
}: Readonly<{
    children: React.ReactNode;
}>) {
    const pathname = usePathname();

    // 현재 페이지 찾기
    const allItems = menuConfig.flatMap(group =>
        group.items.map(item => ({ ...item, group: group.label }))
    );
    const currentPage = allItems.find(item => item.href === pathname);
    
    const router = useRouter();
    const queryClient = useQueryClient();
    const { data: user } = useGetUserMe();
    const logoutMutation = useLogout();

    const handleLogout = async () => {
        try {
            await logoutMutation.mutateAsync({ data: {} });
            queryClient.clear();
            router.push('/login');
        } catch {
            queryClient.clear();
            router.push('/login');
        }
    };

    return (
        <SidebarProvider>
            <Sidebar>
                <SidebarHeader>
                    <Link href="/admin" className="flex items-center gap-2 px-2 py-4">
                        <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary text-primary-foreground">
                            <Video className="h-5 w-5" />
                        </div>
                        <div className="flex flex-col">
                            <span className="text-sm font-semibold">Stream Archive</span>
                            <span className="text-xs text-muted-foreground">관리자</span>
                        </div>
                    </Link>
                </SidebarHeader>
                <SidebarContent>
                    <SidebarMenuContent />
                </SidebarContent>
                <SidebarFooter>
                    <SidebarMenu>
                        <SidebarMenuItem>
                            <DropdownMenu>
                                <DropdownMenuTrigger asChild>
                                    <SidebarMenuButton size="lg" className="data-[state=open]:bg-sidebar-accent data-[state=open]:text-sidebar-accent-foreground">
                                        <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-muted">
                                            <User className="h-4 w-4" />
                                        </div>
                                        <div className="grid flex-1 text-left text-sm leading-tight">
                                            <span className="truncate font-semibold">{user?.name || '사용자'}</span>
                                        </div>
                                        <ChevronUp className="ml-auto size-4" />
                                    </SidebarMenuButton>
                                </DropdownMenuTrigger>
                                <DropdownMenuContent className="w-[var(--radix-dropdown-menu-trigger-width)]">
                                    <DropdownMenuItem asChild>
                                        <Link href="/" target="_blank">
                                            <Home />
                                            <span>메인 화면으로 가기</span>
                                        </Link>
                                    </DropdownMenuItem>
                                    <DropdownMenuItem onClick={handleLogout}>
                                        <LogOut />
                                        <span>로그아웃</span>
                                    </DropdownMenuItem>
                                </DropdownMenuContent>
                            </DropdownMenu>
                        </SidebarMenuItem>
                    </SidebarMenu>
                </SidebarFooter>
            </Sidebar>
            <main className="flex flex-col flex-1 min-w-0">
                <header className="flex h-16 shrink-0 items-center gap-2 px-4">
                    <SidebarTrigger />
                    <Separator orientation="vertical" className="!h-4 mr-2" />
                    <Breadcrumb>
                        <BreadcrumbList>
                            {pathname === "/admin" ? (
                                <BreadcrumbItem>
                                    <BreadcrumbPage>대시보드</BreadcrumbPage>
                                </BreadcrumbItem>
                            ) : currentPage ? (
                                <>
                                    <BreadcrumbItem className="hidden md:block">
                                        <BreadcrumbPage className="text-muted-foreground">{currentPage.group}</BreadcrumbPage>
                                    </BreadcrumbItem>
                                    <BreadcrumbSeparator className="hidden md:block" />
                                    <BreadcrumbItem>
                                        <BreadcrumbPage>{currentPage.title}</BreadcrumbPage>
                                    </BreadcrumbItem>
                                </>
                            ) : null}
                        </BreadcrumbList>
                    </Breadcrumb>
                    <div className="ml-auto">
                        <ModeToggle />
                    </div>
                </header>
                <div className="flex-1 px-6 py-4 md:px-10 md:py-6 min-w-0">
                    {children}
                </div>
            </main>
        </SidebarProvider>
    );
}