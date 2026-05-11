'use client';

import { useState, useEffect, useRef, useCallback } from 'react';
import { VideoPlayer } from '@/app/(app)/videos/[uuid]/video-player';
import { VideoInfo } from '@/components/video-info';
import { ChatHistory } from '@/app/(app)/videos/[uuid]/chat-history';
import { useSaveVideoWatchHistory, useGetVideoWatchHistory, useGetVideoViewerHistory } from '@/lib/api/endpoints/video/video';
import { useGetUserMe } from '@/lib/api/endpoints/user/user';
import { PublicVideoResponse } from "@/lib/api/models";

interface VideoWatchViewProps {
    video: PublicVideoResponse;
}

export function VideoWatchView({ video }: VideoWatchViewProps) {
    const [currentTimeMs, setCurrentTimeMs] = useState(0);
    const [initialPosition, setInitialPosition] = useState<number | null>(null);
    const lastSavedPositionRef = useRef(0);
    const currentPositionRef = useRef(0);
    const { data: user } = useGetUserMe({
        query: {
            retry: false,
        },
    });
    const isAuthenticated = !!user;

    // 시청 기록 조회
    const { data: watchHistory } = useGetVideoWatchHistory(video.uuid, {
        query: {
            enabled: isAuthenticated,
            retry: false,
            staleTime: 0,
        }
    });

    // 시청 위치 저장 mutation
    const saveWatchHistory = useSaveVideoWatchHistory();

    // 라이브 여부: 녹화가 진행 중이면 라이브
    const isLive = !!video.record && !video.record.isEnded && !video.record.isCancelled;

    // 시청 기록이 있으면 초기 위치 설정 (라이브는 이어보기 의미 없음)
    useEffect(() => {
        if (!isLive && watchHistory?.lastPosition && watchHistory.lastPosition > 0) {
            setInitialPosition(watchHistory.lastPosition);
        }
    }, [watchHistory, isLive]);

    // 현재 재생 위치 ref 업데이트
    useEffect(() => {
        currentPositionRef.current = Math.floor(currentTimeMs / 1000);
    }, [currentTimeMs]);

    // 시청 위치 저장 함수
    const savePosition = useCallback((position: number) => {
        if (!isAuthenticated) {
            return;
        }

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
    }, [isAuthenticated, video.uuid, saveWatchHistory]);

    // 10초마다 시청 위치 저장 (라이브는 제외)
    useEffect(() => {
        if (isLive) return;
        const interval = setInterval(() => {
            const currentPosition = currentPositionRef.current;
            if (currentPosition > 0) {
                savePosition(currentPosition);
            }
        }, 10000);

        return () => clearInterval(interval);
    }, [savePosition, isLive]);

    // 시청자 수 이력 조회 (라이브 중에는 주기적 갱신)
    const { data: viewerHistory } = useGetVideoViewerHistory(video.uuid, {
        query: {
            refetchInterval: isLive ? 30_000 : false,
            staleTime: isLive ? 0 : 60_000,
        },
    });

    // 와이드 모드: 사이드바를 가리고 좌측 비디오를 viewport 가득, 우측 채팅 유지
    const [isWide, setIsWide] = useState(false);

    return (
        <div
            className={
                isWide
                    ? 'fixed inset-0 z-[60] bg-background flex flex-col lg:flex-row overflow-hidden'
                    : 'flex flex-col lg:flex-row h-[calc(100vh-4rem)] overflow-hidden'
            }
        >
            {/* 좌측: 동영상 + 정보 */}
            <div className={`flex-shrink-0 lg:flex-1 flex flex-col gap-3 ${isWide ? 'overflow-hidden' : 'overflow-y-auto'}`}>
                <VideoPlayer
                    playlistUrl={video.playlistUrl}
                    onTimeUpdate={setCurrentTimeMs}
                    initialPosition={initialPosition}
                    isLive={isLive}
                    viewerHistory={viewerHistory}
                    isWide={isWide}
                    onWideToggle={setIsWide}
                />
                {!isWide && <VideoInfo video={video} />}
            </div>

            {/* 우측: 채팅창 (와이드 모드에서도 유지) */}
            <div className="flex-1 lg:flex-initial w-full lg:w-88 lg:h-full mt-3 lg:mt-0 min-h-0">
                <ChatHistory videoUuid={video.uuid} currentTimeMs={currentTimeMs} chatSyncOffsetMillis={video.chatSyncOffsetMillis} />
            </div>
        </div>
    );
}
