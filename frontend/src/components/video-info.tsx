'use client';

import Link from 'next/link';
import {Avatar, AvatarFallback, AvatarImage} from '@/components/ui/avatar';
import type {PublicVideoResponse} from '@/lib/api/models';

interface VideoInfoProps {
    video: PublicVideoResponse;
}

export function VideoInfo({ video }: VideoInfoProps) {
    return (
        <div className="px-4 lg:px-0">
            {/* 제목 */}
            <div className="flex flex-col mb-2">
                <h1 className="text-xl font-bold mb-1">{video.title}</h1>
            </div>

            {/* 채널 정보 */}
            <Link
                href={`/channels/${video.channel.uuid}`}
                className="flex items-center gap-3"
            >
                <Avatar className="w-10 h-10">
                    <AvatarImage src={video.channel.profileUrl} />
                    <AvatarFallback>{video.channel.name[0]}</AvatarFallback>
                </Avatar>
                <div>
                    <p className="font-medium">{video.channel.name}</p>
                </div>
            </Link>
        </div>
    );
}