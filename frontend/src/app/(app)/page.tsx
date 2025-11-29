'use client';

import {Avatar, AvatarFallback, AvatarImage} from "@/components/ui/avatar";
import Image from "next/image";
import Link from "next/link";
import {Badge} from "@/components/ui/badge";
import {Skeleton} from "@/components/ui/skeleton";
import {useSearchVideos} from "@/lib/api/endpoints/video/video";

// duration(초)을 HH:MM:SS 형식으로 변환
function formatDuration(seconds: number): string {
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    const secs = seconds % 60;

    if (hours > 0) {
        return `${hours}:${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
    }
    return `${minutes}:${secs.toString().padStart(2, '0')}`;
}

// createdAt을 "n시간 전" 형식으로 변환
function formatTimeAgo(dateString: string): string {
    const now = new Date();
    const createdAt = new Date(dateString);
    const diffInSeconds = Math.floor((now.getTime() - createdAt.getTime()) / 1000);

    const intervals = [
        { label: '년', seconds: 31536000 },
        { label: '개월', seconds: 2592000 },
        { label: '일', seconds: 86400 },
        { label: '시간', seconds: 3600 },
        { label: '분', seconds: 60 },
    ];

    for (const interval of intervals) {
        const count = Math.floor(diffInSeconds / interval.seconds);
        if (count >= 1) {
            return `${count}${interval.label} 전`;
        }
    }

    return '방금 전';
}

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
                        <div key={index} className="flex flex-col gap-3 mb-5">
                            {/* 썸네일 스켈레톤 */}
                            <Skeleton className="aspect-video rounded-lg" />

                            {/* 비디오 정보 스켈레톤 */}
                            <div className="flex gap-3">
                                <Skeleton className="w-9 h-9 rounded-full" />
                                <div className="flex flex-col gap-2 flex-1">
                                    <Skeleton className="h-4 w-full" />
                                    <Skeleton className="h-3 w-2/3" />
                                    <Skeleton className="h-3 w-1/3" />
                                </div>
                            </div>
                        </div>
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
                    <Link
                        key={video.uuid}
                        href={"/video/" + video.uuid}
                        className="flex flex-col gap-3 mb-4 md:mb-5"
                    >
                        <div className="relative aspect-video bg-muted rounded-lg overflow-hidden">
                            <Image
                                src={video.thumbnailUrl}
                                alt={video.title}
                                fill
                                className="object-cover"
                            />
                            {video.record?.isEnded === false ? (
                                <Badge className="absolute right-2 bottom-2 bg-red-600 text-white">LIVE</Badge>
                            ) : (
                                <Badge className="absolute right-2 bottom-2 bg-black/70 text-white">{formatDuration(video.duration)}</Badge>
                            )}
                        </div>
                        <div className="flex gap-3">
                            <Link href={"/channel/" + video.channel.uuid}>
                                <Avatar className="w-9 h-9">
                                    <AvatarImage src={video.channel.profileUrl} />
                                    <AvatarFallback>{video.channel.name[0]}</AvatarFallback>
                                </Avatar>
                            </Link>
                            <div className="flex flex-col">
                                <h3 className="text-md font-medium">{video.title}</h3>
                                <p className="text-sm text-muted-foreground">{video.channel.name}</p>
                                <p className="text-sm text-muted-foreground">{formatTimeAgo(video.createdAt)}</p>
                            </div>
                        </div>
                    </Link>
                ))}
            </div>
        </div>
    );
}