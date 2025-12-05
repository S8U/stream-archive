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
    const videoPlayerRef = useRef<VideoPlayerRef>(null);

    // 전체 채팅 데이터 불러오기 (하이라이트 계산용)
    const { data: allChats, isLoading: isLoadingChats } = useGetVideoChatHistory(
        video.uuid,
        { offsetStart: 0, offsetEnd: video.duration * 1000 },
        {
            query: {
                enabled: showCharts || highlightMode,
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
            rsiThreshold: 65,       // RSI 임계값
            minDurationMs: 10000,   // 최소 하이라이트 길이 10초
            mergeGapMs: 20000       // 20초 이내 구간은 병합
        });
    }, [allChats]);

    // 하이라이트 모드 토글
    const toggleHighlightMode = () => {
        if (!highlightMode && highlightData.highlights.length > 0) {
            // 하이라이트 모드 활성화
            setHighlightMode(true);
            setShowCharts(true);
        } else {
            // 하이라이트 모드 비활성화
            setHighlightMode(false);
        }
    };

    // 비디오 시간 이동 핸들러
    const handleSeek = (timeMs: number) => {
        videoPlayerRef.current?.seekTo(timeMs);
    };

    return (
        <div className="flex flex-col h-[calc(100vh-4rem)] overflow-hidden">
            <div className="flex flex-col lg:flex-row flex-1 overflow-hidden">
                {/* 좌측: 동영상 + 정보 */}
                <div className="flex-shrink-0 lg:flex-1 flex flex-col gap-4 overflow-y-auto">
                    <VideoPlayer
                        ref={videoPlayerRef}
                        playlistUrl={video.playlistUrl}
                        onTimeUpdate={setCurrentTimeMs}
                        highlightMode={highlightMode}
                        highlights={highlightData.highlights}
                    />

                    {/* 하이라이트 버튼 */}
                    <div className="flex gap-2 px-4">
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
                                onClick={() => setShowCharts(!showCharts)}
                                variant="outline"
                                disabled={isLoadingChats}
                            >
                                {showCharts ? '그래프 숨기기' : '그래프 보기'}
                            </Button>
                        )}
                    </div>

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

                {/* 우측: 채팅창 */}
                <div className="flex-1 lg:flex-initial w-full lg:w-88 lg:h-full mt-4 lg:mt-0 min-h-0">
                    <ChatHistory videoUuid={video.uuid} currentTimeMs={currentTimeMs} />
                </div>
            </div>
        </div>
    );
}
