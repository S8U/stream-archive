'use client';

import { useState, useEffect, useRef, useCallback } from 'react';
import { VideoPlayer } from '@/app/(app)/videos/[uuid]/video-player';
import { VideoInfo } from '@/components/video-info';
import { ChatHistory } from '@/app/(app)/videos/[uuid]/chat-history';
import { useSaveVideoWatchHistory, useGetVideoWatchHistory } from '@/lib/api/endpoints/video/video';
import { PublicVideoResponse } from "@/lib/api/models";

interface VideoWatchViewProps {
    video: PublicVideoResponse;
}

export function VideoWatchView({ video }: VideoWatchViewProps) {
    const [currentTimeMs, setCurrentTimeMs] = useState(0);
    const [initialPosition, setInitialPosition] = useState<number | null>(null);
    const lastSavedPositionRef = useRef(0);
    const currentPositionRef = useRef(0);

    // 시청 기록 조회
    const { data: watchHistory } = useGetVideoWatchHistory(video.uuid, {
        query: {
            retry: false,
            staleTime: 0,
        }
    });

    // 시청 위치 저장 mutation
    const saveWatchHistory = useSaveVideoWatchHistory();

    // 시청 기록이 있으면 초기 위치 설정
    useEffect(() => {
        if (watchHistory?.lastPosition && watchHistory.lastPosition > 0) {
            setInitialPosition(watchHistory.lastPosition);
        }
    }, [watchHistory]);

    // 현재 재생 위치 ref 업데이트
    useEffect(() => {
        currentPositionRef.current = Math.floor(currentTimeMs / 1000);
    }, [currentTimeMs]);

    // 시청 위치 저장 함수
    const savePosition = useCallback((position: number) => {
        // 마지막 저장 위치와 동일하면 저장하지 않음
        if (position === lastSavedPositionRef.current || position <= 0) {
            return;
        }

        saveWatchHistory.mutate(
            { uuid: video.uuid, data: { position } },
            {
                onSuccess: () => {
                    lastSavedPositionRef.current = position;
                }
            }
        );
    }, [video.uuid, saveWatchHistory]);

    // 10초마다 시청 위치 저장
    useEffect(() => {
        const interval = setInterval(() => {
            const currentPosition = currentPositionRef.current;
            if (currentPosition > 0) {
                savePosition(currentPosition);
            }
        }, 10000);

        return () => clearInterval(interval);
    }, [savePosition]);

    // 페이지 이탈 시 마지막 위치 저장
    useEffect(() => {
        const handleBeforeUnload = () => {
            const currentPosition = currentPositionRef.current;
            if (currentPosition > 0 && currentPosition !== lastSavedPositionRef.current) {
                // navigator.sendBeacon 사용하여 비동기 요청
                const data = JSON.stringify({ position: currentPosition });
                navigator.sendBeacon(
                    `/api/videos/${video.uuid}/watch-history`,
                    new Blob([data], { type: 'application/json' })
                );
            }
        };

        window.addEventListener('beforeunload', handleBeforeUnload);
        return () => window.removeEventListener('beforeunload', handleBeforeUnload);
    }, [video.uuid]);

    return (
        <div className="flex flex-col lg:flex-row h-[calc(100vh-4rem)] overflow-hidden">
            {/* 좌측: 동영상 + 정보 */}
            <div className="flex-shrink-0 lg:flex-1 flex flex-col gap-4 overflow-y-auto">
                <VideoPlayer
                    playlistUrl={video.playlistUrl}
                    onTimeUpdate={setCurrentTimeMs}
                    initialPosition={initialPosition}
                />
                <VideoInfo video={video} />
            </div>

            {/* 우측: 채팅창 */}
            <div className="flex-1 lg:flex-initial w-full lg:w-88 lg:h-full mt-4 lg:mt-0 min-h-0">
                <ChatHistory videoUuid={video.uuid} currentTimeMs={currentTimeMs} chatSyncOffsetMillis={video.chatSyncOffsetMillis} />
            </div>
        </div>
    );
}

