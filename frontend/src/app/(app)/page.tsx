import type { Metadata } from "next";
import { searchVideos } from "@/lib/api/endpoints/video/video";
import { VideoCard } from "@/components/video-card";
import { CustomPagination } from "@/components/common/custom-pagination";

export const metadata: Metadata = {
    title: "홈",
    description: "최신 동영상 목록을 확인하세요",
};

type Props = {
    searchParams: Promise<{ page?: string; q?: string }>;
};

export default async function Home({ searchParams }: Props) {
    const params = await searchParams;
    const page = Math.max(0, Number(params.page || 1) - 1);
    const size = 20;
    const query = params.q?.trim();

    const data = await searchVideos({
        request: query ? { title: query } : {},
        pageable: {
            page: page + 1,
            size,
        }
    });

    const videos = data?.content || [];
    const totalPages = data?.totalPages || 0;

    return (
        <div className="p-4">
            {/* 동영상 목록 */}
            {videos.length > 0 ? (
                <>
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

                    {/* Pagination */}
                    <CustomPagination totalPages={totalPages} />
                </>
            ) : (
                <div className="text-center py-12">
                    <p className="text-muted-foreground">등록된 동영상이 없습니다.</p>
                </div>
            )}
        </div>
    );
}