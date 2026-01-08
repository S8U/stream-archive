"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { Clock, HomeIcon, Library, ThumbsUp } from "lucide-react";
import { Separator } from "@/components/ui/separator";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { SheetClose } from "@/components/ui/sheet";
import { PublicChannelResponse } from "@/lib/api/models";
import { cn } from "@/lib/utils";

interface SidebarContentProps {
    channels: PublicChannelResponse[];
    isSheet?: boolean;
}

export function SidebarContent({ channels, isSheet = false }: SidebarContentProps) {
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

    return (
        <div className="flex flex-col h-full py-4">
            {/* 메인 메뉴 */}
            <div className="px-3 mb-4">
                <LinkWrapper
                    href="/"
                    className={cn(
                        "flex items-center gap-4 px-3 py-2 rounded-lg transition",
                        isActive("/") ? "bg-secondary" : "hover:bg-secondary"
                    )}
                >
                    <HomeIcon className="w-5 h-5" />
                    <span>홈</span>
                </LinkWrapper>

                <LinkWrapper
                    href="/my/history"
                    className={cn(
                        "flex items-center gap-4 px-3 py-2 rounded-lg transition",
                        isActive("/my/history") ? "bg-secondary" : "hover:bg-secondary"
                    )}
                >
                    <Clock className="w-5 h-5" />
                    <span>시청 기록</span>
                </LinkWrapper>
            </div>

            <Separator className="mb-4" />

            {/* 채널 목록 */}
            <div className="px-3 overflow-y-auto">
                <h3 className="px-3 mb-2 text-sm font-semibold text-muted-foreground">채널</h3>
                {channels.map((channel) => {
                    const channelHref = "/channels/" + channel.uuid;
                    return (
                        <LinkWrapper
                            key={channel.uuid}
                            href={channelHref}
                            className={cn(
                                "flex items-center gap-4 px-3 py-2 rounded-lg transition",
                                isActive(channelHref) ? "bg-secondary" : "hover:bg-secondary"
                            )}
                        >
                            <Avatar className="w-8 h-8">
                                <AvatarImage src={channel.profileUrl} />
                                <AvatarFallback>{channel.name[0]}</AvatarFallback>
                            </Avatar>
                            <span>{channel.name}</span>
                        </LinkWrapper>
                    );
                })}
            </div>
        </div>
    );
}
