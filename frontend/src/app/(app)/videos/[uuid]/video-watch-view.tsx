'use client';

import { useState, useRef, useMemo } from 'react';
import { VideoPlayer, type VideoPlayerRef } from '@/app/(app)/videos/[uuid]/video-player';
import { VideoInfo } from '@/components/video-info';
import { ChatHistory } from '@/app/(app)/videos/[uuid]/chat-history';
import { HighlightCharts } from '@/app/(app)/videos/[uuid]/highlight-charts';
import { Button } from '@/components/ui/button';
import { Sparkles } from 'lucide-react';
import type { PublicVideoResponse } from '@/lib/api/models';
import { useGetVideoChatHistory } from '@/lib/api/endpoints/video/video';
import { generateHighlights } from '@/lib/utils/highlight-utils';

interface VideoWatchViewProps {
    video: PublicVideoResponse;
}

export function VideoWatchView({ video }: VideoWatchViewProps) {
    const [currentTimeMs, setCurrentTimeMs] = useState(0);
    const [highlightMode, setHighlightMode] = useState(false);
    const [showCharts, setShowCharts] = useState(false);
    const [capturedOffsetEnd, setCapturedOffsetEnd] = useState<number | null>(null);
    const videoPlayerRef = useRef<VideoPlayerRef>(null);

    // 채팅 조회 범위 계산 (버튼 누를 당시 고정)
    const chatOffsetEnd = useMemo(() => {
        if (capturedOffsetEnd !== null) {
            return capturedOffsetEnd;
        }
        // 기본값 (사용되지 않음)
        return Math.floor(video.duration > 0 ? video.duration * 1000 : 0);
    }, [capturedOffsetEnd, video.duration]);

    // 전체 채팅 데이터 불러오기 (하이라이트 계산용)
    const { data: allChats, isLoading: isLoadingChats } = useGetVideoChatHistory(
        video.uuid,
        { offsetStart: 0, offsetEnd: chatOffsetEnd },
        {
            query: {
                enabled: (showCharts || highlightMode) && capturedOffsetEnd !== null,
            }
        }
    );

    // 하이라이트 계산
    const highlightData = useMemo(() => {
        if (!allChats || allChats.length === 0) {
            return { chatBuckets: [], rsiData: [], highlights: [] };
        }

        return generateHighlights(allChats, {
            intervalMs: 10000,      // 10초 단위
            rsiPeriod: 14,          // RSI 기간
            rsiThreshold: 60,       // RSI 피크 임계값
            rsiBaseThreshold: 50,   // RSI 구간 시작/종료 임계값
            minDurationMs: 10000,   // 최소 하이라이트 길이 10초
            mergeGapMs: 20000       // 20초 이내 구간은 병합
        });
    }, [allChats]);

    // 하이라이트 모드 토글
    const toggleHighlightMode = () => {
        if (!highlightMode && highlightData.highlights.length > 0) {
            // 하이라이트 모드 활성화
            if (capturedOffsetEnd === null) {
                // 첫 활성화 시 현재 영상 길이 캡처
                const offset = video.duration > 0
                    ? Math.floor(video.duration * 1000)
                    : Math.floor(currentTimeMs + 60000);
                setCapturedOffsetEnd(offset);
            }
            setHighlightMode(true);
            setShowCharts(true);
        } else {
            // 하이라이트 모드 비활성화
            setHighlightMode(false);
        }
    };

    // 그래프 보기 토글
    const toggleCharts = () => {
        if (!showCharts && capturedOffsetEnd === null) {
            // 첫 활성화 시 현재 영상 길이 캡처
            const offset = video.duration > 0
                ? Math.floor(video.duration * 1000)
                : Math.floor(currentTimeMs + 60000);
            setCapturedOffsetEnd(offset);
        }
        setShowCharts(!showCharts);
    };

    // 비디오 시간 이동 핸들러
    const handleSeek = (timeMs: number) => {
        videoPlayerRef.current?.seekTo(timeMs);
    };

    return (
        <div className="flex flex-col h-[calc(100vh-4rem)] overflow-hidden">
            <div className="flex flex-col lg:flex-row flex-1 overflow-hidden">
                {/* 좌측: 동영상 + 정보 */}
                <div className="flex-shrink-0 lg:flex-1 flex flex-col overflow-hidden">
                    {/* 상단 고정 영역: 비디오 플레이어 + 버튼 */}
                    <div className="flex-shrink-0">
                        <VideoPlayer
                            ref={videoPlayerRef}
                            playlistUrl={video.playlistUrl}
                            onTimeUpdate={setCurrentTimeMs}
                            highlightMode={highlightMode}
                            highlights={highlightData.highlights}
                        />

                        {/* 하이라이트 버튼 */}
                        <div className="flex gap-2 px-4 py-4">
                            <Button
                                onClick={toggleHighlightMode}
                                variant={highlightMode ? "default" : "outline"}
                                disabled={isLoadingChats || (showCharts && highlightData.highlights.length === 0)}
                                className="gap-2"
                            >
                                <Sparkles className="h-4 w-4" />
                                {isLoadingChats
                                    ? '로딩 중...'
                                    : highlightMode
                                        ? '일반 모드'
                                        : `하이라이트 보기${highlightData.highlights.length > 0 ? ` (${highlightData.highlights.length})` : ''}`
                                }
                            </Button>

                            {!highlightMode && (
                                <Button
                                    onClick={toggleCharts}
                                    variant="outline"
                                    disabled={isLoadingChats}
                                >
                                    {showCharts ? '그래프 숨기기' : '그래프 보기'}
                                </Button>
                            )}
                        </div>
                    </div>

                    {/* 하단 스크롤 영역: 비디오 정보 + 그래프 */}
                    <div className="flex-1 overflow-y-auto">
                        <div className="space-y-4 pb-4">
                            <VideoInfo video={video} />

                            {/* 하이라이트 그래프 */}
                            {showCharts && highlightData.chatBuckets.length > 0 && (
                                <div className="px-4">
                                    <HighlightCharts
                                        chatBuckets={highlightData.chatBuckets}
                                        rsiData={highlightData.rsiData}
                                        highlights={highlightData.highlights}
                                        currentTimeMs={currentTimeMs}
                                        onSeek={handleSeek}
                                    />
                                </div>
                            )}

                            {showCharts && highlightData.chatBuckets.length === 0 && !isLoadingChats && (
                                <div className="px-4 py-8 text-center text-muted-foreground">
                                    채팅 데이터가 없어 하이라이트를 생성할 수 없습니다.
                                </div>
                            )}
                        </div>
                    </div>
                </div>

                {/* 우측: 채팅창 */}
                <div className="flex-1 lg:flex-initial w-full lg:w-88 lg:h-full mt-4 lg:mt-0 min-h-0">
                    <ChatHistory videoUuid={video.uuid} currentTimeMs={currentTimeMs} />
                </div>
            </div>
        </div>
    );
}
