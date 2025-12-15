'use client';

import { Button } from '@/components/ui/button';
import { HighlightResponse } from '@/lib/api/models';
import { Flame, ChevronLeft, ChevronRight, X } from 'lucide-react';

interface HighlightModeControlsProps {
    highlights: HighlightResponse[];
    isEnabled: boolean;
    currentIndex: number;
    onToggle: () => void;
    onPrevious: () => void;
    onNext: () => void;
    onExit: () => void;
}

/**
 * 하이라이트 모드 컨트롤 UI
 * 하이라이트 모드 토글, 이전/다음 이동, 진행 상태 표시
 */
export function HighlightModeControls({
    highlights,
    isEnabled,
    currentIndex,
    onToggle,
    onPrevious,
    onNext,
    onExit
}: HighlightModeControlsProps) {
    if (highlights.length === 0) {
        return null;
    }

    // 시간을 mm:ss 형식으로 포맷
    const formatTime = (ms: number): string => {
        const totalSeconds = Math.floor(ms / 1000);
        const minutes = Math.floor(totalSeconds / 60);
        const seconds = totalSeconds % 60;
        return `${minutes}:${seconds.toString().padStart(2, '0')}`;
    };

    if (!isEnabled) {
        // 하이라이트 모드 비활성화 상태 - secondary 토글 버튼
        return (
            <div className="flex items-center gap-2 mt-2">
                <Button
                    variant="secondary"
                    onClick={onToggle}
                    className="gap-2"
                >
                    <Flame className="h-4 w-4" />
                    <span>하이라이트</span>
                    <span className="text-xs text-muted-foreground">({highlights.length})</span>
                </Button>
            </div>
        );
    }

    // 하이라이트 모드 활성화 상태
    const currentHighlight = highlights[currentIndex];
    
    return (
        <div className="flex items-center gap-3 bg-secondary border-t border-border px-4 py-2">
            {/* 모드 아이콘 */}
            <div className="flex items-center gap-2">
                <Flame className="h-4 w-4" />
                <span className="text-sm font-medium">하이라이트</span>
            </div>

            {/* 구분선 */}
            <div className="h-4 w-px bg-border" />

            {/* 이전/다음 버튼 */}
            <div className="flex items-center gap-0.5">
                <Button
                    variant="ghost"
                    size="icon"
                    onClick={onPrevious}
                    disabled={currentIndex <= 0}
                    className="h-7 w-7"
                >
                    <ChevronLeft className="h-4 w-4" />
                </Button>

                {/* 진행 상태 */}
                <div className="text-sm min-w-[50px] text-center">
                    <span className="font-medium">{currentIndex + 1}</span>
                    <span className="text-muted-foreground">/</span>
                    <span className="text-muted-foreground">{highlights.length}</span>
                </div>

                <Button
                    variant="ghost"
                    size="icon"
                    onClick={onNext}
                    disabled={currentIndex >= highlights.length - 1}
                    className="h-7 w-7"
                >
                    <ChevronRight className="h-4 w-4" />
                </Button>
            </div>

            {/* 현재 하이라이트 정보 */}
            {currentHighlight && (
                <>
                    <div className="h-4 w-px bg-border" />
                    <div className="text-xs text-muted-foreground">
                        {formatTime(currentHighlight.startOffsetMillis)} ~ {formatTime(currentHighlight.endOffsetMillis)}
                        <span className="ml-1.5">
                            ({currentHighlight.chatCount.toLocaleString()} 채팅)
                        </span>
                    </div>
                </>
            )}

            {/* 종료 버튼 */}
            <Button
                variant="ghost"
                size="icon"
                onClick={onExit}
                className="h-7 w-7 ml-auto"
            >
                <X className="h-4 w-4" />
            </Button>
        </div>
    );
}
