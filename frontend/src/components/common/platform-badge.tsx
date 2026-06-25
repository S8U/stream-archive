import { Badge } from "@/components/ui/badge";
import { cn } from "@/lib/utils";

interface PlatformBadgeProps {
    platform: string;
    className?: string;
}

const PLATFORM_BADGE_CLASSES: Record<string, string> = {
    CHZZK: "border-emerald-200 bg-emerald-100 text-emerald-700 hover:bg-emerald-100/80 dark:border-emerald-800 dark:bg-emerald-950/60 dark:text-emerald-300",
    TWITCH: "border-violet-200 bg-violet-100 text-violet-700 hover:bg-violet-100/80 dark:border-violet-800 dark:bg-violet-950/60 dark:text-violet-300",
    SOOP: "border-sky-200 bg-sky-100 text-sky-700 hover:bg-sky-100/80 dark:border-sky-800 dark:bg-sky-950/60 dark:text-sky-300",
    YOUTUBE: "border-red-200 bg-red-100 text-red-700 hover:bg-red-100/80 dark:border-red-800 dark:bg-red-950/60 dark:text-red-300",
};

export function getPlatformLabel(platform: string): string {
    switch (platform) {
        case "CHZZK":
            return "치지직";
        case "TWITCH":
            return "트위치";
        case "SOOP":
            return "SOOP";
        case "YOUTUBE":
            return "유튜브";
        default:
            return platform;
    }
}

function getPlatformBadgeClass(platform: string): string {
    return PLATFORM_BADGE_CLASSES[platform] ?? "border-zinc-200 bg-zinc-100 text-zinc-700 hover:bg-zinc-100/80 dark:border-zinc-700 dark:bg-zinc-800/70 dark:text-zinc-200";
}

export function PlatformBadge({ platform, className }: PlatformBadgeProps) {
    return (
        <Badge variant="outline" className={cn(getPlatformBadgeClass(platform), className)}>
            {getPlatformLabel(platform)}
        </Badge>
    );
}
