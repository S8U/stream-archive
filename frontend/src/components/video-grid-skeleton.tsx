import { Skeleton } from '@/components/ui/skeleton';

interface VideoGridSkeletonProps {
    /** 표시할 스켈레톤 카드 개수 */
    count?: number;
}

/** 동영상 카드 그리드의 로딩 스켈레톤. 목록 페이지들과 동일한 그리드 규격을 쓴다. */
export function VideoGridSkeleton({ count = 12 }: VideoGridSkeletonProps) {
    return (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-x-4 gap-y-8">
            {Array.from({ length: count }, (_, index) => (
                <div key={index} className="flex flex-col gap-3">
                    <Skeleton className="aspect-video w-full rounded-lg" />
                    <div className="flex gap-3">
                        <Skeleton className="h-9 w-9 shrink-0 rounded-full" />
                        <div className="flex min-w-0 flex-1 flex-col gap-2">
                            <Skeleton className="h-4 w-full" />
                            <Skeleton className="h-4 w-2/3" />
                        </div>
                    </div>
                </div>
            ))}
        </div>
    );
}
