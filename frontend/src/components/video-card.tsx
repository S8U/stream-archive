'use client';

import { useState } from 'react';
import Link from 'next/link';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Badge } from '@/components/ui/badge';
import { VideoPreview } from '@/components/video-preview';

interface VideoCardProps {
    uuid: string;
    title: string;
    thumbnailUrl: string;
    playlistUrl: string;
    duration: number;
    createdAt: string;
    channel: {
        uuid: string;
        name: string;
        profileUrl: string;
    };
    record?: {
        isEnded: boolean;
    };
}

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

export function VideoCard({ uuid, title, thumbnailUrl, playlistUrl, duration, createdAt, channel, record }: VideoCardProps) {
    const [hoveredVideoUuid, setHoveredVideoUuid] = useState<string | null>(null);

    return (
        <Link
            key={uuid}
            href={`/videos/${uuid}`}
            className="flex flex-col gap-3 mb-4 md:mb-5"
            onMouseEnter={() => setHoveredVideoUuid(uuid)}
            onMouseLeave={() => setHoveredVideoUuid(null)}
        >
            <div className="relative aspect-video rounded-lg overflow-hidden">
                <VideoPreview
                    thumbnailUrl={thumbnailUrl}
                    playlistUrl={playlistUrl}
                    title={title}
                    isHovered={hoveredVideoUuid === uuid}
                />
                {record?.isEnded === false ? (
                    <Badge className="absolute right-2 bottom-2 rounded bg-red-600 text-white">LIVE</Badge>
                ) : (
                    <Badge className="absolute right-2 bottom-2 rounded bg-black/70 text-white">{formatDuration(duration)}</Badge>
                )}
            </div>
            <div className="flex gap-3">
                <Link href={`/channels/${channel.uuid}`}>
                    <Avatar className="w-9 h-9">
                        <AvatarImage src={channel.profileUrl} />
                        <AvatarFallback>{channel.name[0]}</AvatarFallback>
                    </Avatar>
                </Link>
                <div className="flex flex-col">
                    <h3 className="text-md font-medium line-clamp-2">{title}</h3>
                    <p className="text-sm text-muted-foreground">{channel.name}</p>
                    <p className="text-sm text-muted-foreground">{formatTimeAgo(createdAt)}</p>
                </div>
            </div>
        </Link>
    );
}