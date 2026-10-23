'use client';

import { useState } from 'react';
import Link from 'next/link';
import { Bookmark, LinkIcon } from 'lucide-react';
import { Badge } from '@/components/ui/badge';
import { ChannelAvatar } from '@/components/channel-avatar';
import { VideoPreview } from '@/components/video-preview';

interface VideoCardProps {
    uuid: string;
    title: string;
    thumbnailUrl: string;
    playlistUrl: string;
    duration: number;
    createdAt: string;
    isArchived?: boolean;
    /** 현재 사용자의 시청 진행률 (%). 비로그인·미시청이면 생략 */
    progress?: number | null;
    contentPrivacy: 'PUBLIC' | 'UNLISTED' | 'PRIVATE';
    channel: {
        uuid: string;
        name: string;
        profileUrl: string;
        contentPrivacy: 'PUBLIC' | 'UNLISTED' | 'PRIVATE';
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

export function VideoCard({ uuid, title, thumbnailUrl, playlistUrl, duration, createdAt, isArchived, progress, contentPrivacy, channel, record }: VideoCardProps) {
    const [hoveredVideoUuid, setHoveredVideoUuid] = useState<string | null>(null);

    const isLive = record?.isEnded === false;
    // 라이브가 아니고 진행률이 있을 때만 이어보기 바를 표시한다.
    const showProgress = !isLive && progress != null && progress > 0;
    const clampedProgress = Math.min(100, Math.max(0, progress ?? 0));

    return (
        <div
            className="relative flex flex-col gap-3 mb-4 md:mb-5"
            onMouseEnter={() => setHoveredVideoUuid(uuid)}
            onMouseLeave={() => setHoveredVideoUuid(null)}
        >
            <Link href={`/videos/${uuid}`} className="absolute inset-0 z-10" aria-label={title} />
            <div className="relative aspect-video rounded-lg overflow-hidden">
                <VideoPreview
                    thumbnailUrl={thumbnailUrl}
                    playlistUrl={playlistUrl}
                    title={title}
                    isHovered={hoveredVideoUuid === uuid}
                    isLive={isLive}
                />
                {isLive ? (
                    <Badge className="absolute right-2 bottom-2 rounded bg-red-600 text-white">LIVE</Badge>
                ) : (
                    <Badge className="absolute right-2 bottom-2 rounded bg-black/70 text-white">{formatDuration(duration)}</Badge>
                )}

                {/* 이어보기 진행률 바 */}
                {showProgress && (
                    <div className="absolute bottom-0 left-0 right-0 h-1 bg-white/30">
                        <div className="h-full bg-red-600" style={{ width: `${clampedProgress}%` }} />
                    </div>
                )}
            </div>
            <div className="flex gap-3">
                <Link href={`/channels/${channel.uuid}`} className="relative z-20">
                    <ChannelAvatar channel={channel} className="w-9 h-9" />
                </Link>
                <div className="flex flex-col min-w-0">
                    {/* 제목 뒤 아이콘은 제목 글자와 따로 둔다. 제목이 두 줄을 넘어 말줄임돼도 아이콘은 첫 줄 오른쪽에 남는다. */}
                    <h3 className="flex items-start gap-1 text-md font-medium">
                        <span className="line-clamp-2">{title}</span>
                        {(contentPrivacy === 'UNLISTED' || isArchived) && (
                            <span className="flex h-6 shrink-0 items-center gap-1 text-muted-foreground">
                                {/* 일부공개 표시. 관리자가 일부공개 보기를 켰을 때만 목록에 섞여 나온다. */}
                                {contentPrivacy === 'UNLISTED' && (
                                    <LinkIcon size={13} role="img" aria-label="일부공개 동영상" />
                                )}
                                {isArchived && (
                                    <Bookmark size={13} className="opacity-50" fill="currentColor" />
                                )}
                            </span>
                        )}
                    </h3>
                    <Link href={`/channels/${channel.uuid}`} className="relative z-20 text-sm text-muted-foreground hover:text-foreground">
                        {channel.name}
                    </Link>
                    <p className="text-sm text-muted-foreground">{formatTimeAgo(createdAt)}</p>
                </div>
            </div>
        </div>
    );
}
