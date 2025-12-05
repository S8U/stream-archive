'use client';

import { useState } from 'react';
import { VideoPlayer } from '@/app/(app)/videos/[uuid]/video-player';
import { VideoInfo } from '@/components/video-info';
import { ChatHistory } from '@/app/(app)/videos/[uuid]/chat-history';
import type { PublicVideoResponse } from '@/lib/api/models';

interface VideoWatchViewProps {
    video: PublicVideoResponse;
}

export function VideoWatchView({ video }: VideoWatchViewProps) {
    const [currentTimeMs, setCurrentTimeMs] = useState(0);

    return (
        <div className="flex flex-col lg:flex-row h-[calc(100vh-4rem)] overflow-hidden">
            {/* 좌측: 동영상 + 정보 */}
            <div className="flex-shrink-0 lg:flex-1 flex flex-col gap-4 overflow-y-auto">
                <VideoPlayer
                    playlistUrl={video.playlistUrl}
                    onTimeUpdate={setCurrentTimeMs}
                />
                <VideoInfo video={video} />
            </div>

            {/* 우측: 채팅창 */}
            <div className="flex-1 lg:flex-initial w-full lg:w-88 lg:h-full mt-4 lg:mt-0 min-h-0">
                <ChatHistory videoUuid={video.uuid} currentTimeMs={currentTimeMs} />
            </div>
        </div>
    );
}
