import type { Metadata } from "next";
import { searchVideos } from "@/lib/api/endpoints/video/video";
import { VideoCard } from "@/components/video-card";

export const metadata: Metadata = {
    title: "홈",
    description: "최신 동영상 목록을 확인하세요",
};

export default async function Home() {
    const data = await searchVideos({
        request: {},
        pageable: {
            page: 1,
            size: 20,
        }
    });

    const videos = data?.content || [];

    return (
        <div className="p-4">
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