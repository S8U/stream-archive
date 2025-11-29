'use client';

import { useGetChannelByUuid } from '@/lib/api/endpoints/channel/channel';
import { useSearchVideos } from '@/lib/api/endpoints/video/video';
import { ChannelHeader } from '@/components/channel-header';
import { VideoCard } from '@/components/video-card';
import { VideoCardSkeleton } from '@/components/video-card-skeleton';
import { Skeleton } from '@/components/ui/skeleton';

export default function ChannelPage({
    params,
}: {
    params: { uuid: string };
}) {
    const { uuid } = params;
    const { data: channel, isLoading: isChannelLoading, isError: isChannelError } = useGetChannelByUuid(uuid, {
        query: {
            retry: false,
        },
    });

    const { data: videosData, isLoading: isVideosLoading } = useSearchVideos({
        request: {
            channelUuid: uuid,
        },
        pageable: {
            page: 1,
            size: 20,
        },
    });

    // 404 에러
    if (isChannelError) {
        return (
            <div className="flex items-center justify-center h-[calc(100vh-4rem)]">
                <div className="text-center">
                    <h1 className="text-2xl font-bold mb-2">채널을 찾을 수 없습니다</h1>
                    <p className="text-muted-foreground">요청하신 채널이 존재하지 않거나 삭제되었습니다.</p>
                </div>
            </div>
        );
    }

    // 로딩 중일 때 스켈레톤 UI 표시
    if (isChannelLoading || !channel) {
        return (
            <div className="p-4">
                {/* 채널 헤더 스켈레톤 */}
                <div className="flex items-center gap-6 mb-8">
                    <Skeleton className="w-32 h-32 rounded-full" />
                    <div className="flex flex-col gap-2">
                        <Skeleton className="h-8 w-48" />
                        <Skeleton className="h-4 w-32" />
                    </div>
                </div>

                {/* 동영상 목록 스켈레톤 */}
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
                    {Array.from({ length: 8 }).map((_, index) => (
                        <VideoCardSkeleton key={index} />
                    ))}
                </div>
            </div>
        );
    }

    const videos = videosData?.content || [];

    return (
        <div className="p-4">
            {/* 채널 헤더 */}
            <ChannelHeader channel={channel} />

            {/* 동영상 목록 */}
            <div className="mt-8">
                {isVideosLoading ? (
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
                        {Array.from({ length: 8 }).map((_, index) => (
                            <VideoCardSkeleton key={index} />
                        ))}
                    </div>
                ) : videos.length > 0 ? (
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
                        {videos.map((video) => (
                            <VideoCard
                                key={video.uuid}
                                uuid={video.uuid}
                                title={video.title}
                                thumbnailUrl={video.thumbnailUrl}
                                playlistUrl={video.playlistUrl}
                                duration={video.duration}
                                createdAt={video.createdAt}
                                channel={video.channel}
                                record={video.record}
                            />
                        ))}
                    </div>
                ) : (
                    <div className="text-center py-12">
                        <p className="text-muted-foreground">아직 동영상이 없습니다.</p>
                    </div>
                )}
            </div>
        </div>
    );
}