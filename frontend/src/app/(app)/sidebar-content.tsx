"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { Clock, HomeIcon } from "lucide-react";
import { Separator } from "@/components/ui/separator";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { SheetClose } from "@/components/ui/sheet";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Tooltip, TooltipContent, TooltipTrigger } from "@/components/ui/tooltip";
import { ChannelGetResponse } from "@/lib/api/models";
import { cn } from "@/lib/utils";

interface SidebarContentProps {
    channels: ChannelGetResponse[];
    isSheet?: boolean;
    // 접힌 상태(아이콘만 노출). PC 사이드바에서만 사용한다.
    collapsed?: boolean;
}

export function SidebarContent({ channels, isSheet = false, collapsed = false }: SidebarContentProps) {
    const pathname = usePathname();

    const LinkWrapper = isSheet
        ? ({ children, ...props }: React.ComponentProps<typeof Link>) => (
            <SheetClose asChild>
                <Link {...props}>{children}</Link>
            </SheetClose>
        )
        : Link;

    // 현재 경로가 링크와 일치하는지 확인
    const isActive = (href: string) => {
        if (href === "/") {
            return pathname === "/";
        }
        return pathname === href || pathname.startsWith(href + "/");
    };

    const menuItems = [
        { href: "/", icon: HomeIcon, label: "홈" },
        { href: "/my/history", icon: Clock, label: "시청 기록" },
    ];

    // 아이콘 슬롯. 폭 40px(w-10)을 유지해 펼침/접힘 모두 아이콘 중심 X(40px)를 고정한다.
    // 높이는 32px(h-8)로 줄여 항목이 세로로 너무 커지지 않게 한다. (가로 정렬은 폭 기준이라 영향 없음)
    const iconSlot = "flex w-10 h-8 shrink-0 items-center justify-center";

    return (
        <div className={cn("flex flex-col h-full py-4", collapsed ? "px-2.5" : "px-3")}>
            {/* 메인 메뉴 */}
            <div className="mb-4">
                {menuItems.map(({ href, icon: Icon, label }) => (
                    <LinkWrapper
                        key={href}
                        href={href}
                        className={cn(
                            "rounded-lg transition py-1.5",
                            collapsed
                                ? "flex flex-col items-center gap-0.5"
                                : "flex items-center gap-3 px-2",
                            isActive(href) ? "bg-secondary" : "hover:bg-secondary"
                        )}
                    >
                        <span className={iconSlot}>
                            <Icon className="w-6 h-6" />
                        </span>
                        <span className={cn(collapsed && "text-xs leading-tight text-center break-keep")}>
                            {label}
                        </span>
                    </LinkWrapper>
                ))}
            </div>

            <Separator className="mb-4" />

            {/* 채널 목록 */}
            <ScrollArea className="flex-1 min-h-0" hideScrollbar>
                <h3
                    className={cn(
                        "mb-2 text-sm font-semibold text-muted-foreground",
                        collapsed ? "text-center text-xs" : "px-2"
                    )}
                >
                    채널
                </h3>
                {channels.map((channel) => {
                    const channelHref = "/channels/" + channel.uuid;
                    const link = (
                        <LinkWrapper
                            key={channel.uuid}
                            href={channelHref}
                            className={cn(
                                "rounded-lg transition py-1.5",
                                collapsed ? "flex justify-center" : "flex items-center gap-3 px-2",
                                isActive(channelHref) ? "bg-secondary" : "hover:bg-secondary"
                            )}
                        >
                            <span className={iconSlot}>
                                <Avatar className="w-8 h-8">
                                    <AvatarImage src={channel.profileUrl} />
                                    <AvatarFallback>{channel.name[0]}</AvatarFallback>
                                </Avatar>
                            </span>
                            {!collapsed && <span>{channel.name}</span>}
                        </LinkWrapper>
                    );

                    // 접힌 상태에서는 채널명을 tooltip으로 보여준다.
                    if (collapsed) {
                        return (
                            <Tooltip key={channel.uuid}>
                                <TooltipTrigger asChild>{link}</TooltipTrigger>
                                <TooltipContent side="right">{channel.name}</TooltipContent>
                            </Tooltip>
                        );
                    }

                    return link;
                })}
            </ScrollArea>
        </div>
    );
}
