"use client";

import { cn } from "@/lib/utils";
import { useSidebar } from "./sidebar-context";

export function MainContent({ children }: { children: React.ReactNode }) {
    const { isOpen } = useSidebar();

    return (
        <div
            className={cn(
                "transition-[margin] duration-200 ease-in-out",
                isOpen ? "md:ml-60" : "md:ml-20"
            )}
        >
            {children}
        </div>
    );
}
