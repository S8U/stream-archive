"use client";

import { ChannelGetResponse } from "@/lib/api/models";
import { cn } from "@/lib/utils";
import { SidebarContent } from "./sidebar-content";
import { useSidebar } from "./sidebar-context";

interface PcSidebarProps {
    channels: ChannelGetResponse[];
}

export function PcSidebar({ channels }: PcSidebarProps) {
    const { isOpen } = useSidebar();

    return (
        <div
            className={cn(
                "hidden md:block fixed h-full transition-[width] duration-200 ease-in-out overflow-hidden",
                isOpen ? "w-60" : "w-20"
            )}
        >
            <SidebarContent channels={channels} collapsed={!isOpen} />
        </div>
    );
}
