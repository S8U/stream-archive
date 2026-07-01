import { Skeleton } from '@/components/ui/skeleton';

export default function Loading() {
    return (
        <div className="flex h-[calc(100vh-3.5rem)] flex-col overflow-hidden lg:flex-row">
            {/* 좌측: 플레이어 + 정보 자리 */}
            <div className="flex flex-1 flex-col">
                <Skeleton className="w-full flex-1 rounded-none" />
                <div className="flex flex-col gap-3 p-3">
                    <Skeleton className="h-6 w-2/3" />
                    <div className="flex items-center gap-3">
                        <Skeleton className="h-10 w-10 rounded-full" />
                        <Skeleton className="h-4 w-32" />
                    </div>
                </div>
            </div>
            {/* 우측: 채팅 자리 */}
            <div className="hidden lg:block lg:w-88 lg:border-l" />
        </div>
    );
}
