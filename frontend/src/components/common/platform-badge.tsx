import {Badge} from "@/components/ui/badge";
import {cn} from "@/lib/utils";

interface PlatformBadgeProps {
    platform: string;
    className?: string;
}

export function PlatformBadge({ platform, className }: PlatformBadgeProps) {
    const getPlatformLabel = (platform: string): string => {
        switch (platform) {
            case "CHZZK":
                return "치지직";
            case "TWITCH":
                return "트위치";
            case "SOOP":
                return "SOOP";
            default:
                return platform;
        }
    };

    const getPlatformClasses = (platform: string): string => {
        switch (platform) {
            case "CHZZK":
                return "bg-emerald-200/60 hover:bg-emerald-200/80 text-emerald-800 border-0";
            case "TWITCH":
                return "bg-blue-200/50 hover:bg-blue-200/70 text-blue-700 border-0";
            case "SOOP":
                return "bg-sky-200/50 hover:bg-sky-200/70 text-sky-700 border-0";
            default:
                return "bg-neutral-100 hover:bg-neutral-200 text-neutral-700 border-0";
        }
    };

    return (
        <Badge
            variant="outline"
            className={cn(
                getPlatformClasses(platform),
                className
            )}
        >
            {getPlatformLabel(platform)}
        </Badge>
    );
}
