import type { Metadata } from "next";
import { notFound } from "next/navigation";
import { getChannelByUuid } from '@/lib/api/endpoints/channel/channel';
import { searchVideos } from '@/lib/api/endpoints/video/video';
import { ChannelHeader } from '@/components/channel-header';
import { VideoCard } from '@/components/video-card';

type Props = {
    params: { uuid: string };
};

export async function generateMetadata({ params }: Props): Promise<Metadata> {
    const { uuid } = params;

    try {
        const channel = await getChannelByUuid(uuid);

        return {
            title: channel.name,
            description: `${channel.name} 채널 동영상 아카이브`,
            openGraph: {
                title: channel.name,
                description: `${channel.name} 채널 동영상 아카이브`,
                type: "profile",
                images: [
                    {
                        url: channel.profileUrl,
                        width: 400,
                        height: 400,
                        alt: channel.name,
                    },
                ],
            },
            twitter: {
                card: "summary",
                title: channel.name,
                description: `${channel.name}의 동영상 아카이브`,
                images: [channel.profileUrl],
            },
        };
    } catch {
        return {
            title: "채널을 찾을 수 없습니다",
            description: "요청하신 채널이 존재하지 않거나 삭제되었습니다.",
        };
    }
}

export default async function ChannelPage({ params }: Props) {
    const { uuid } = params;

    let channel;
    try {
        channel = await getChannelByUuid(uuid);
    } catch {
        notFound();
    }

    const videosData = await searchVideos({
        request: {
            channelUuid: uuid,
        },
        pageable: {
            page: 1,
            size: 20,
        },
    });

    const videos = videosData.content || [];

    return (
        <div className="p-4">
            {/* 채널 헤더 */}
            <ChannelHeader channel={channel} />

            {/* 동영상 목록 */}
            <div className="mt-8">
                {videos.length > 0 ? (
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