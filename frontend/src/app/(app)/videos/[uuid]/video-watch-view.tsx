'use client';

import { useState } from 'react';
import { VideoPlayer } from '@/app/(app)/videos/[uuid]/video-player';
import { VideoInfo } from '@/components/video-info';
import { ChatHistory } from '@/app/(app)/videos/[uuid]/chat-history';
import { useGetVideoHighlights } from '@/lib/api/endpoints/video/video';
import type { PublicVideoResponse, HighlightSegment } from '@/lib/api/models';

interface VideoWatchViewProps {
    video: PublicVideoResponse;
}

export function VideoWatchView({ video }: VideoWatchViewProps) {
    const [currentTimeMs, setCurrentTimeMs] = useState(0);
    const [highlightMode, setHighlightMode] = useState(false);
    const [currentSegmentIndex, setCurrentSegmentIndex] = useState(0);

    // 하이라이트 데이터 로드
    const { data: highlightsData } = useGetVideoHighlights(video.uuid, {});

    const highlights: HighlightSegment[] = highlightsData?.highlights || [];
    const hasHighlights = highlights.length > 0;

    const handleHighlightModeToggle = () => {
        if (!hasHighlights) return;

        const newMode = !highlightMode;
        setHighlightMode(newMode);

        if (newMode) {
            // 하이라이트 모드 시작: 첫 번째 하이라이트로 이동
            setCurrentSegmentIndex(0);
        }
    };

    const handleJumpToHighlight = (index: number) => {
        if (index >= 0 && index < highlights.length) {
            setCurrentSegmentIndex(index);
        }
    };

    return (
        <div className="flex flex-col lg:flex-row h-[calc(100vh-4rem)] overflow-hidden">
            {/* 좌측: 동영상 + 정보 */}
            <div className="flex-shrink-0 lg:flex-1 flex flex-col gap-4 overflow-y-auto">
                <div className="flex flex-col gap-2">
                    <VideoPlayer
                        playlistUrl={video.playlistUrl}
                        onTimeUpdate={setCurrentTimeMs}
                        highlights={highlights}
                        highlightMode={highlightMode}
                        currentSegmentIndex={currentSegmentIndex}
                        onSegmentEnd={() => {
                            if (currentSegmentIndex < highlights.length - 1) {
                                setCurrentSegmentIndex(currentSegmentIndex + 1);
                            } else {
                                setHighlightMode(false);
                            }
                        }}
                    />

                    {/* 하이라이트 컨트롤 */}
                    {hasHighlights && (
                        <div className="flex items-center gap-2 px-4 py-2 bg-neutral-100 dark:bg-neutral-800 rounded-lg">
                            <button
                                onClick={handleHighlightModeToggle}
                                className={`px-4 py-2 rounded-lg font-medium transition-colors ${
                                    highlightMode
                                        ? 'bg-primary text-primary-foreground'
                                        : 'bg-neutral-200 dark:bg-neutral-700 hover:bg-neutral-300 dark:hover:bg-neutral-600'
                                }`}
                            >
                                {highlightMode ? '⚡ 하이라이트 모드 종료' : '⚡ 하이라이트만 보기'}
                            </button>

                            {highlightMode && (
                                <>
                                    <button
                                        onClick={() => handleJumpToHighlight(currentSegmentIndex - 1)}
                                        disabled={currentSegmentIndex === 0}
                                        className="px-3 py-2 rounded-lg bg-neutral-200 dark:bg-neutral-700 hover:bg-neutral-300 dark:hover:bg-neutral-600 disabled:opacity-50 disabled:cursor-not-allowed"
                                    >
                                        ← 이전
                                    </button>

                                    <span className="text-sm font-medium">
                                        {currentSegmentIndex + 1} / {highlights.length}
                                    </span>

                                    <button
                                        onClick={() => handleJumpToHighlight(currentSegmentIndex + 1)}
                                        disabled={currentSegmentIndex >= highlights.length - 1}
                                        className="px-3 py-2 rounded-lg bg-neutral-200 dark:bg-neutral-700 hover:bg-neutral-300 dark:hover:bg-neutral-600 disabled:opacity-50 disabled:cursor-not-allowed"
                                    >
                                        다음 →
                                    </button>
                                </>
                            )}

                            <div className="ml-auto text-sm text-neutral-600 dark:text-neutral-400">
                                {highlights.length}개 하이라이트 · {Math.floor((highlightsData?.totalHighlightDurationMs || 0) / 1000 / 60)}분
                            </div>
                        </div>
                    )}
                </div>

                <VideoInfo video={video} />
            </div>

            {/* 우측: 채팅창 */}
            <div className="flex-1 lg:flex-initial w-full lg:w-88 lg:h-full mt-4 lg:mt-0 min-h-0">
                <ChatHistory videoUuid={video.uuid} currentTimeMs={currentTimeMs} />
            </div>
        </div>
    );
}
