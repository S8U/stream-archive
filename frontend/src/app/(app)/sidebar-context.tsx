"use client";

import { createContext, useContext, useEffect, useState } from "react";

const STORAGE_KEY = "pc-sidebar-open";

interface SidebarContextValue {
    isOpen: boolean;
    toggle: () => void;
}

const SidebarContext = createContext<SidebarContextValue | null>(null);

export function SidebarProvider({ children }: { children: React.ReactNode }) {
    // SSR 일관성을 위해 기본값(열림)으로 시작하고, 마운트 후 localStorage 값으로 동기화
    const [isOpen, setIsOpen] = useState(true);

    useEffect(() => {
        const stored = localStorage.getItem(STORAGE_KEY);
        if (stored !== null) {
            setIsOpen(stored === "true");
        }
    }, []);

    const toggle = () => {
        setIsOpen((prev) => {
            const next = !prev;
            localStorage.setItem(STORAGE_KEY, String(next));
            return next;
        });
    };

    return (
        <SidebarContext.Provider value={{ isOpen, toggle }}>
            {children}
        </SidebarContext.Provider>
    );
}

export function useSidebar() {
    const context = useContext(SidebarContext);
    if (!context) {
        throw new Error("useSidebar는 SidebarProvider 안에서만 사용할 수 있습니다.");
    }
    return context;
}
