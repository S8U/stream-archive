'use client';

import Link from 'next/link';
import {Avatar, AvatarFallback, AvatarImage} from '@/components/ui/avatar';
import {PlatformBadge} from '@/components/common/platform-badge';
import {ChevronDown} from 'lucide-react';
import {useState} from 'react';
import type {PublicVideoResponse} from '@/lib/api/models';

interface VideoInfoProps {
    video: PublicVideoResponse;
}

export function VideoInfo({ video }: VideoInfoProps) {
    const [isExpanded, setIsExpanded] = useState(false);
    const recordedAt = video.record?.startedAt ?? video.createdAt;
    const platformType = video.record?.platformType;

    const formatDateTime = (dateString: string) => {
        return new Date(dateString).toLocaleString('ko-KR', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
        });
    };

    const formatFileSize = (bytes: number) => {
        if (bytes === 0) return '0 B';
        const k = 1024;
        const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return `${parseFloat((bytes / Math.pow(k, i)).toFixed(2))} ${sizes[i]}`;
    };

    const stats = [
        { label: '녹화일', value: formatDateTime(recordedAt) },
        { label: '용량', value: formatFileSize(video.fileSize) },
        { label: '최고 시청자수', value: video.peakViewerCount != null ? video.peakViewerCount.toLocaleString('ko-KR') : '-' },
    ];

    return (
        <div className="px-4 lg:px-0">
            {/* 제목 */}
            <button
                type="button"
                className="mb-2 flex w-full items-start justify-between gap-3 text-left lg:pointer-events-none"
                onClick={() => setIsExpanded((prev) => !prev)}
                aria-expanded={isExpanded}
            >
                <h1 className="min-w-0 flex-1 text-xl font-bold">{video.title}</h1>
                <ChevronDown
                    className={`mt-1 h-5 w-5 flex-shrink-0 text-muted-foreground transition-transform lg:hidden ${isExpanded ? 'rotate-180' : ''}`}
                    aria-hidden="true"
                />
            </button>

            {/* 채널 정보 */}
            <Link
                href={`/channels/${video.channel.uuid}`}
                className="inline-flex w-fit max-w-full items-center gap-3"
            >
                <Avatar className="w-10 h-10">
                    <AvatarImage src={video.channel.profileUrl} />
                    <AvatarFallback>{video.channel.name[0]}</AvatarFallback>
                </Avatar>
                <div>
                    <p className="font-medium">{video.channel.name}</p>
                </div>
            </Link>

            <div className={`${isExpanded ? 'block' : 'hidden'} mt-2 mb-4 rounded-md bg-muted px-3 py-3 lg:block`}>
                {video.description && (
                    <p className="mb-3 whitespace-pre-wrap text-sm leading-6">{video.description}</p>
                )}
                <dl className="grid gap-x-4 gap-y-2 text-sm sm:grid-cols-2 xl:grid-cols-4">
                    {stats.map((stat) => (
                        <div key={stat.label} className="min-w-0">
                            <dt className="text-muted-foreground">{stat.label}</dt>
                            <dd className="mt-1 truncate font-medium">{stat.value}</dd>
                        </div>
                    ))}
                    <div className="min-w-0">
                        <dt className="text-muted-foreground">플랫폼</dt>
                        <dd className="mt-1">
                            {platformType ? <PlatformBadge platform={platformType} /> : <span className="font-medium">-</span>}
                        </dd>
                    </div>
                </dl>
            </div>
        </div>
    );
}
