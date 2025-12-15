'use client';

import { HighlightResponse } from '@/lib/api/models';
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from '@/components/ui/tooltip';

interface HighlightBarProps {
    highlights: HighlightResponse[];
    duration: number;              // 영상 전체 길이 (초)
    onSeek: (timeMs: number) => void;
    currentTimeMs?: number;        // 현재 재생 위치 (밀리초)
}

/**
 * 하이라이트 바 컴포넌트
 * 비디오 플레이어 하단에 하이라이트 구간을 시각적으로 표시합니다.
 */
export function HighlightBar({ highlights, duration, onSeek, currentTimeMs = 0 }: HighlightBarProps) {
    if (highlights.length === 0 || duration <= 0) {
        return null;
    }

    const durationMs = duration * 1000;

    // 시간을 mm:ss 형식으로 포맷
    const formatTime = (ms: number): string => {
        const totalSeconds = Math.floor(ms / 1000);
        const minutes = Math.floor(totalSeconds / 60);
        const seconds = totalSeconds % 60;
        return `${minutes}:${seconds.toString().padStart(2, '0')}`;
    };

    // intensity에 따른 색상 계산 (0.0: 주황색 → 1.0: 빨간색)
    const getIntensityColor = (intensity: number): string => {
        // HSL: 주황(30) → 빨강(0)
        const hue = Math.round(30 * (1 - intensity));
        const saturation = 85 + intensity * 15; // 85% → 100%
        const lightness = 55 - intensity * 15;  // 55% → 40%
        return `hsl(${hue}, ${saturation}%, ${lightness}%)`;
    };

    return (
        <div className="w-full h-3 bg-neutral-800 relative overflow-hidden group cursor-pointer">
            {/* 현재 재생 위치 표시선 */}
            <div
                className="absolute top-0 bottom-0 w-0.5 bg-white z-20 pointer-events-none"
                style={{ left: `${(currentTimeMs / durationMs) * 100}%` }}
            />

            {/* 하이라이트 구간들 */}
            <TooltipProvider delayDuration={100}>
                {highlights.map((highlight, index) => {
                    const left = (highlight.startOffsetMillis / durationMs) * 100;
                    const width = ((highlight.endOffsetMillis - highlight.startOffsetMillis) / durationMs) * 100;

                    return (
                        <Tooltip key={index}>
                            <TooltipTrigger asChild>
                                <div
                                    className="absolute top-0 bottom-0 transition-all duration-200 hover:scale-y-125 hover:brightness-125"
                                    style={{
                                        left: `${left}%`,
                                        width: `${Math.max(width, 0.5)}%`,
                                        backgroundColor: getIntensityColor(highlight.intensity),
                                    }}
                                    onClick={(e) => {
                                        e.stopPropagation();
                                        onSeek(highlight.peakOffsetMillis);
                                    }}
                                />
                            </TooltipTrigger>
                            <TooltipContent side="top" className="bg-neutral-900 border-neutral-700">
                                <div className="text-sm">
                                    <div className="font-medium text-orange-400">🔥 하이라이트 #{index + 1}</div>
                                    <div className="text-neutral-300 mt-1">
                                        {formatTime(highlight.startOffsetMillis)} ~ {formatTime(highlight.endOffsetMillis)}
                                    </div>
                                    <div className="text-neutral-400">
                                        채팅 {highlight.chatCount.toLocaleString()}개
                                    </div>
                                </div>
                            </TooltipContent>
                        </Tooltip>
                    );
                })}
            </TooltipProvider>

            {/* 전체 영역 클릭 시 해당 위치로 이동 */}
            <div
                className="absolute inset-0 z-10"
                onClick={(e) => {
                    const rect = e.currentTarget.getBoundingClientRect();
                    const clickX = e.clientX - rect.left;
                    const percent = clickX / rect.width;
                    onSeek(percent * durationMs);
                }}
            />
        </div>
    );
}
