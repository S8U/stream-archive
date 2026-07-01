"use client";

import { Menu } from "lucide-react";
import { Button } from "@/components/ui/button";
import { useSidebar } from "./sidebar-context";

export function SidebarToggle() {
    const { toggle } = useSidebar();

    return (
        <Button
            variant="ghost"
            size="icon-lg"
            className="hidden md:flex"
            onClick={toggle}
            aria-label="사이드바 토글"
        >
            <Menu className="size-5" />
        </Button>
    );
}
