import { Badge } from "@/components/ui/badge";
import { cn } from "@/lib/utils";
import type { ReactNode } from "react";

type AdminBadgeTone =
    | "neutral"
    | "success"
    | "warning"
    | "danger"
    | "info";

interface AdminBadgeProps {
    children: ReactNode;
    tone?: AdminBadgeTone;
    className?: string;
}

const ADMIN_BADGE_TONE_CLASSES: Record<AdminBadgeTone, string> = {
    neutral: "border-zinc-200 bg-zinc-100 text-zinc-700 hover:bg-zinc-100/80 dark:border-zinc-700 dark:bg-zinc-800/70 dark:text-zinc-200",
    success: "border-emerald-200 bg-emerald-100 text-emerald-700 hover:bg-emerald-100/80 dark:border-emerald-800 dark:bg-emerald-950/60 dark:text-emerald-300",
    warning: "border-amber-200 bg-amber-100 text-amber-800 hover:bg-amber-100/80 dark:border-amber-800 dark:bg-amber-950/60 dark:text-amber-300",
    danger: "border-red-200 bg-red-100 text-red-700 hover:bg-red-100/80 dark:border-red-800 dark:bg-red-950/60 dark:text-red-300",
    info: "border-sky-200 bg-sky-100 text-sky-700 hover:bg-sky-100/80 dark:border-sky-800 dark:bg-sky-950/60 dark:text-sky-300",
};

export function adminBadgeToneClass(tone: AdminBadgeTone = "neutral") {
    return ADMIN_BADGE_TONE_CLASSES[tone];
}

export function AdminBadge({ children, tone = "neutral", className }: AdminBadgeProps) {
    return (
        <Badge variant="outline" className={cn(adminBadgeToneClass(tone), className)}>
            {children}
        </Badge>
    );
}

export function getPrivacyLabel(privacy: string): string {
    switch (privacy) {
        case "PUBLIC":
            return "공개";
        case "UNLISTED":
            return "일부공개";
        case "PRIVATE":
            return "비공개";
        default:
            return privacy;
    }
}

export function getPrivacyBadgeTone(privacy: string): AdminBadgeTone {
    switch (privacy) {
        case "PUBLIC":
            return "success";
        case "UNLISTED":
            return "warning";
        case "PRIVATE":
            return "neutral";
        default:
            return "neutral";
    }
}
