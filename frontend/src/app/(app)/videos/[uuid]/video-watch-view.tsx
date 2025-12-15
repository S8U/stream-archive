'use client';

import { useState, useEffect, useRef, useCallback } from 'react';
import { VideoPlayer, VideoPlayerHandle } from '@/app/(app)/videos/[uuid]/video-player';
import { VideoInfo } from '@/components/video-info';
import { ChatHistory } from '@/app/(app)/videos/[uuid]/chat-history';
import { HighlightBar } from '@/app/(app)/videos/[uuid]/highlight-bar';
import { HighlightModeControls } from '@/app/(app)/videos/[uuid]/highlight-mode-controls';
import { useSaveVideoWatchHistory, useGetVideoWatchHistory, useGetVideoHighlights } from '@/lib/api/endpoints/video/video';
import { PublicVideoResponse } from "@/lib/api/models";

interface VideoWatchViewProps {
    video: PublicVideoResponse;
}

export function VideoWatchView({ video }: VideoWatchViewProps) {
    const [currentTimeMs, setCurrentTimeMs] = useState(0);
    const [initialPosition, setInitialPosition] = useState<number | null>(null);
    const lastSavedPositionRef = useRef(0);
    const currentPositionRef = useRef(0);
    const videoPlayerRef = useRef<VideoPlayerHandle>(null);

    // 하이라이트 모드 상태
    const [highlightModeEnabled, setHighlightModeEnabled] = useState(false);
    const [currentHighlightIndex, setCurrentHighlightIndex] = useState(0);

    // 시청 기록 조회
    const { data: watchHistory } = useGetVideoWatchHistory(video.uuid, {
        query: {
            retry: false,
            staleTime: 0,
        }
    });

    // 시청 위치 저장 mutation
    const saveWatchHistory = useSaveVideoWatchHistory();

    // 하이라이트 데이터 조회
    const { data: highlights = [] } = useGetVideoHighlights(video.uuid, {
        query: {
            staleTime: 1000 * 60 * 5, // 5분 캐시
        }
    });

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

    // 하이라이트 모드: 구간 종료 시 다음 하이라이트로 자동 이동
    useEffect(() => {
        if (!highlightModeEnabled || highlights.length === 0) return;

        const currentHighlight = highlights[currentHighlightIndex];
        if (!currentHighlight) return;

        // 현재 하이라이트 구간 종료 확인
        if (currentTimeMs >= currentHighlight.endOffsetMillis) {
            if (currentHighlightIndex < highlights.length - 1) {
                // 다음 하이라이트로 이동
                const nextIndex = currentHighlightIndex + 1;
                setCurrentHighlightIndex(nextIndex);
                videoPlayerRef.current?.seek(highlights[nextIndex].startOffsetMillis);
            } else {
                // 마지막 하이라이트 종료 - 모드 종료
                setHighlightModeEnabled(false);
            }
        }
    }, [currentTimeMs, highlightModeEnabled, highlights, currentHighlightIndex]);

    // 특정 시간으로 이동
    const handleSeek = useCallback((timeMs: number) => {
        videoPlayerRef.current?.seek(timeMs);
    }, []);

    // 하이라이트 모드 토글
    const handleToggleHighlightMode = useCallback(() => {
        if (!highlightModeEnabled && highlights.length > 0) {
            // 모드 활성화 - 첫 번째 하이라이트로 이동
            setHighlightModeEnabled(true);
            setCurrentHighlightIndex(0);
            videoPlayerRef.current?.seek(highlights[0].startOffsetMillis);
            videoPlayerRef.current?.play();
        } else {
            // 모드 비활성화
            setHighlightModeEnabled(false);
        }
    }, [highlightModeEnabled, highlights]);

    // 이전 하이라이트로 이동
    const handlePreviousHighlight = useCallback(() => {
        if (currentHighlightIndex > 0) {
            const prevIndex = currentHighlightIndex - 1;
            setCurrentHighlightIndex(prevIndex);
            videoPlayerRef.current?.seek(highlights[prevIndex].startOffsetMillis);
        }
    }, [currentHighlightIndex, highlights]);

    // 다음 하이라이트로 이동
    const handleNextHighlight = useCallback(() => {
        if (currentHighlightIndex < highlights.length - 1) {
            const nextIndex = currentHighlightIndex + 1;
            setCurrentHighlightIndex(nextIndex);
            videoPlayerRef.current?.seek(highlights[nextIndex].startOffsetMillis);
        }
    }, [currentHighlightIndex, highlights]);

    // 하이라이트 모드 종료
    const handleExitHighlightMode = useCallback(() => {
        setHighlightModeEnabled(false);
    }, []);

    return (
        <div className="flex flex-col lg:flex-row h-[calc(100vh-4rem)] overflow-hidden">
            {/* 좌측: 동영상 + 정보 */}
            <div className="flex-shrink-0 lg:flex-1 flex flex-col overflow-y-auto">
                {/* 비디오 플레이어 */}
                <VideoPlayer
                    ref={videoPlayerRef}
                    playlistUrl={video.playlistUrl}
                    onTimeUpdate={setCurrentTimeMs}
                    initialPosition={initialPosition}
                />

                {/* 하이라이트 바 - 하이라이트 모드일 때만 표시 */}
                {highlightModeEnabled && highlights.length > 0 && (
                    <HighlightBar
                        highlights={highlights}
                        duration={video.duration}
                        onSeek={handleSeek}
                        currentTimeMs={currentTimeMs}
                    />
                )}

                {/* 하이라이트 모드 컨트롤 */}
                {highlights.length > 0 && (
                    <HighlightModeControls
                        highlights={highlights}
                        isEnabled={highlightModeEnabled}
                        currentIndex={currentHighlightIndex}
                        onToggle={handleToggleHighlightMode}
                        onPrevious={handlePreviousHighlight}
                        onNext={handleNextHighlight}
                        onExit={handleExitHighlightMode}
                    />
                )}

                {/* 비디오 정보 */}
                <div className="mt-4">
                    <VideoInfo video={video} />
                </div>
            </div>

            {/* 우측: 채팅창 */}
            <div className="flex-1 lg:flex-initial w-full lg:w-88 lg:h-full mt-4 lg:mt-0 min-h-0">
                <ChatHistory videoUuid={video.uuid} currentTimeMs={currentTimeMs} chatSyncOffsetMillis={video.chatSyncOffsetMillis} />
            </div>
        </div>
    );
}
