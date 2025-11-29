'use client';

import { useGetVideoByUuid } from '@/lib/api/endpoints/video/video';
import { VideoPlayer } from '@/components/video-player';
import { VideoInfo } from '@/components/video-info';
import { ChatHistory } from '@/components/chat-history';
import { Skeleton } from '@/components/ui/skeleton';

export default function VideoPage({
    params,
}: {
    params: { uuid: string };
}) {
    const { uuid } = params;
    const { data: video, isLoading, isError } = useGetVideoByUuid(uuid, {
        query: {
            retry: false,
        },
    });

    if (isError) {
        return (
            <div className="flex items-center justify-center h-[calc(100vh-4rem)]">
                <div className="text-center">
                    <h1 className="text-2xl font-bold mb-2">동영상을 찾을 수 없습니다</h1>
                    <p className="text-muted-foreground">요청하신 동영상이 존재하지 않거나 삭제되었습니다.</p>
                </div>
            </div>
        );
    }

    // 로딩 중일 때 스켈레톤 UI 표시
    if (isLoading || !video) {
        return (
            <div className="flex flex-col lg:flex-row gap-4 h-[calc(100vh-4rem)]">
                {/* 좌측 */}
                <div className="flex-1 flex flex-col gap-4">
                    <Skeleton className="aspect-video w-full" />
                    <Skeleton className="h-32 w-full" />
                </div>
                {/* 우측 */}
                <div className="w-full lg:w-88 h-96 lg:h-full">
                    <Skeleton className="h-full w-full" />
                </div>
            </div>
        );
    }

    return (
        <div className="flex flex-col lg:flex-row h-[calc(100vh-4rem)] overflow-hidden">
            {/* 좌측: 동영상 + 정보 */}
            <div className="flex-shrink-0 lg:flex-1 flex flex-col gap-4 overflow-y-auto">
                <VideoPlayer playlistUrl={video.playlistUrl} />
                <VideoInfo video={video} />
            </div>

            {/* 우측: 채팅창 */}
            <div className="flex-1 lg:flex-initial w-full lg:w-88 lg:h-full mt-4 lg:mt-0 min-h-0">
                <ChatHistory videoUuid={uuid} />
            </div>
        </div>
    );
}