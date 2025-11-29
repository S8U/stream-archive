'use client';

import {useSearchVideos} from "@/lib/api/endpoints/video/video";
import {VideoCard} from "@/components/video-card";
import {VideoCardSkeleton} from "@/components/video-card-skeleton";

export default function Home() {

    // API 호출
    const { data, isLoading, error } = useSearchVideos({
        request: {},
        pageable: {
            page: 1,
            size: 20,
        }
    });

    // 로딩 중일 때 스켈레톤 UI 표시
    if (isLoading || error) {
        return (
            <div>
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 2xl:grid-cols-5 gap-4">
                    {Array.from({ length: 20 }).map((_, index) => (
                        <VideoCardSkeleton key={index} />
                    ))}
                </div>
            </div>
        );
    }

    const videos = data?.content || [];

    return (
        <div>
            {/* 동영상 목록 */}
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
        </div>
    );
}