import { Skeleton } from '@/components/ui/skeleton';

export function VideoCardSkeleton() {
    return (
        <div className="flex flex-col gap-3 mb-5">
            {/* 썸네일 스켈레톤 */}
            <Skeleton className="aspect-video rounded-lg" />

            {/* 비디오 정보 스켈레톤 */}
            <div className="flex gap-3">
                <Skeleton className="w-9 h-9 rounded-full" />
                <div className="flex flex-col gap-2 flex-1">
                    <Skeleton className="h-4 w-full" />
                    <Skeleton className="h-3 w-2/3" />
                    <Skeleton className="h-3 w-1/3" />
                </div>
            </div>
        </div>
    );
}