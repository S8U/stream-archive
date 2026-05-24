"use client";

import { Menu } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Sheet, SheetContent, SheetTrigger } from "@/components/ui/sheet";
import { PublicChannelResponse } from "@/lib/api/models";
import { SidebarContent } from "./sidebar-content";

interface MobileSidebarProps {
    channels: PublicChannelResponse[];
}

export function MobileSidebar({ channels }: MobileSidebarProps) {
    return (
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
    );
}
