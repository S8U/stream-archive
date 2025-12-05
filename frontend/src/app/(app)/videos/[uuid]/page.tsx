import type { Metadata } from "next";
import { notFound } from "next/navigation";
import { getVideoByUuid } from '@/lib/api/endpoints/video/video';
import { VideoWatchView } from '@/app/(app)/videos/[uuid]/video-watch-view';

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

    return <VideoWatchView video={video} />;
}