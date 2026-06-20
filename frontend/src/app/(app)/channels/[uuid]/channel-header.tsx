'use client';

import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { PlatformBadge } from '@/components/common/platform-badge';
import { useGetChannelPlatforms, useGetChannelStats } from '@/lib/api/endpoints/channel/channel';
import { Skeleton } from '@/components/ui/skeleton';
import { ChannelGetResponse } from "@/lib/api/models";

interface ChannelHeaderProps {
    channel: ChannelGetResponse;
}

/**
 * 파일 크기를 사람이 읽기 쉬운 형식으로 변환
 */
function formatBytes(bytes: number): string {
    if (bytes === 0) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return `${parseFloat((bytes / Math.pow(k, i)).toFixed(1))} ${sizes[i]}`;
}

export function ChannelHeader({ channel }: ChannelHeaderProps) {
    const { data: stats, isLoading: isStatsLoading } = useGetChannelStats(channel.uuid);
    const { data: platforms, isLoading: isPlatformsLoading } = useGetChannelPlatforms(channel.uuid);

    return (
        <div className="flex items-center gap-4 sm:gap-6">
            <Avatar className="w-20 h-20 sm:w-32 sm:h-32 shrink-0">
                <AvatarImage src={channel.profileUrl} />
                <AvatarFallback className="text-4xl">{channel.name[0]}</AvatarFallback>
            </Avatar>
            <div>
                <h1 className="text-2xl sm:text-3xl font-bold mb-1.5">{channel.name}</h1>
                <div className="flex flex-col gap-y-1.5 text-sm sm:text-base text-muted-foreground">
                    {/* 동영상 개수 · 총 용량 */}
                    <div className="flex items-center gap-x-3">
                        {isStatsLoading ? (
                            <Skeleton className="h-5 w-20" />
                        ) : (
                            <span>동영상 {stats?.videoCount ?? 0}개</span>
                        )}
                        <span>•</span>
                        {isStatsLoading ? (
                            <Skeleton className="h-5 w-16" />
                        ) : (
                            <span>{formatBytes(stats?.totalFileSize ?? 0)}</span>
                        )}
                    </div>
                    {/* 플랫폼 링크 */}
                    {isPlatformsLoading ? (
                        <Skeleton className="h-5 w-24" />
                    ) : (
                        <div className="flex gap-2">
                            {platforms?.map((platform) => (
                                <a
                                    key={platform.platformType}
                                    href={platform.streamUrl}
                                    target="_blank"
                                    rel="noopener noreferrer"
                                >
                                    <PlatformBadge platform={platform.platformType} />
                                </a>
                            ))}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}