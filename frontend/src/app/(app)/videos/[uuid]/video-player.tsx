'use client';

import { useEffect, useRef } from 'react';
import Hls from 'hls.js';
import type { HighlightSegment } from '@/lib/api/models';

interface VideoPlayerProps {
    playlistUrl: string;
    onTimeUpdate?: (currentTimeMs: number) => void;
    highlights?: HighlightSegment[];
    highlightMode?: boolean;
    currentSegmentIndex?: number;
    onSegmentEnd?: () => void;
}

export function VideoPlayer({
    playlistUrl,
    onTimeUpdate,
    highlights = [],
    highlightMode = false,
    currentSegmentIndex = 0,
    onSegmentEnd,
}: VideoPlayerProps) {
    const videoRef = useRef<HTMLVideoElement>(null);
    const hlsRef = useRef<Hls | null>(null);

    useEffect(() => {
        if (!videoRef.current) return;

        const video = videoRef.current;

        // Safari 네이티브 HLS 지원
        if (video.canPlayType('application/vnd.apple.mpegurl')) {
            video.src = playlistUrl;
        }
        // 다른 브라우저는 hls.js 사용
        else if (Hls.isSupported()) {
            const hls = new Hls({
                enableWorker: true,
                lowLatencyMode: false,
            });

            hlsRef.current = hls;
            hls.loadSource(playlistUrl);
            hls.attachMedia(video);

            hls.on(Hls.Events.ERROR, (_event, data) => {
                if (data.fatal) {
                    console.error('HLS fatal error:', data);
                }
            });
        } else {
            console.error('HLS is not supported in this browser');
        }

        return () => {
            if (hlsRef.current) {
                hlsRef.current.destroy();
                hlsRef.current = null;
            }
        };
    }, [playlistUrl]);

    // 하이라이트 모드 활성화 시 현재 하이라이트 구간으로 이동
    useEffect(() => {
        if (!videoRef.current || !highlightMode || highlights.length === 0) return;

        const currentSegment = highlights[currentSegmentIndex];
        if (currentSegment) {
            videoRef.current.currentTime = currentSegment.startOffsetMs / 1000;
        }
    }, [highlightMode, currentSegmentIndex, highlights]);

    // 하이라이트 모드에서 현재 구간 종료 시 다음 구간으로 이동
    useEffect(() => {
        if (!videoRef.current || !highlightMode || highlights.length === 0) return;

        const video = videoRef.current;
        const currentSegment = highlights[currentSegmentIndex];
        if (!currentSegment) return;

        const handleTimeUpdate = () => {
            const currentTimeMs = video.currentTime * 1000;

            if (currentTimeMs >= currentSegment.endOffsetMs) {
                onSegmentEnd?.();
            }
        };

        video.addEventListener('timeupdate', handleTimeUpdate);

        return () => {
            video.removeEventListener('timeupdate', handleTimeUpdate);
        };
    }, [highlightMode, currentSegmentIndex, highlights, onSegmentEnd]);

    return (
        <div className="relative aspect-video bg-black overflow-hidden">
            <video
                ref={videoRef}
                controls
                className="w-full h-full"
                onTimeUpdate={(e) => {
                    if (onTimeUpdate) {
                        onTimeUpdate(e.currentTarget.currentTime * 1000);
                    }
                }}
            />

            {/* 하이라이트 타임라인 오버레이 */}
            {highlights.length > 0 && (
                <div className="absolute bottom-16 left-0 right-0 h-1 bg-transparent pointer-events-none">
                    {highlights.map((segment, index) => {
                        const video = videoRef.current;
                        const totalDuration = video?.duration || 1;
                        const left = (segment.startOffsetMs / 1000 / totalDuration) * 100;
                        const width = ((segment.endOffsetMs - segment.startOffsetMs) / 1000 / totalDuration) * 100;

                        return (
                            <div
                                key={index}
                                className="absolute h-full bg-yellow-500 opacity-70"
                                style={{
                                    left: `${left}%`,
                                    width: `${width}%`,
                                }}
                                title={`하이라이트 ${index + 1}: ${Math.floor((segment.endOffsetMs - segment.startOffsetMs) / 1000)}초`}
                            />
                        );
                    })}
                </div>
            )}
        </div>
    );
}
