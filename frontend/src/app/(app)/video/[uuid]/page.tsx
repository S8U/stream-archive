import type { Metadata } from "next";
import { notFound } from "next/navigation";
import { getVideoByUuid } from '@/lib/api/endpoints/video/video';
import { VideoPlayer } from '@/components/video-player';
import { VideoInfo } from '@/components/video-info';
import { ChatHistory } from '@/components/chat-history';

type Props = {
    params: { uuid: string };
};

export async function generateMetadata({ params }: Props): Promise<Metadata> {
    const { uuid } = params;

    try {
        const video = await getVideoByUuid(uuid);

        return {
            title: video.title,
            description: `${video.channel.name} 채널 동영상`,
            openGraph: {
                title: video.title,
                description: `${video.channel.name} 채널 동영상`,
                type: "video.other",
                images: [
                    {
                        url: video.thumbnailUrl,
                        width: 1280,
                        height: 720,
                        alt: video.title,
                    },
                ],
            },
            twitter: {
                card: "summary_large_image",
                title: video.title,
                description: `${video.channel.name}의 동영상`,
                images: [video.thumbnailUrl],
            },
        };
    } catch {
        return {
            title: "동영상을 찾을 수 없습니다",
            description: "요청하신 동영상이 존재하지 않거나 삭제되었습니다.",
        };
    }
}

export default async function VideoPage({ params }: Props) {
    const { uuid } = params;

    let video;
    try {
        video = await getVideoByUuid(uuid);
    } catch {
        notFound();
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