'use client';

import { useEffect, useRef, useImperativeHandle, forwardRef } from 'react';
import Hls from 'hls.js';
import type { HighlightSegment } from '@/lib/utils/highlight-utils';

interface VideoPlayerProps {
    playlistUrl: string;
    onTimeUpdate?: (currentTimeMs: number) => void;
    highlightMode?: boolean;
    highlights?: HighlightSegment[];
}

export interface VideoPlayerRef {
    seekTo: (timeMs: number) => void;
    getCurrentTime: () => number;
}

export const VideoPlayer = forwardRef<VideoPlayerRef, VideoPlayerProps>(
    ({ playlistUrl, onTimeUpdate, highlightMode = false, highlights = [] }, ref) => {
        const videoRef = useRef<HTMLVideoElement>(null);
        const hlsRef = useRef<Hls | null>(null);
        const currentHighlightIndexRef = useRef(0);

        // 외부에서 비디오 제어할 수 있도록 메서드 노출
        useImperativeHandle(ref, () => ({
            seekTo: (timeMs: number) => {
                if (videoRef.current) {
                    videoRef.current.currentTime = timeMs / 1000;
                }
            },
            getCurrentTime: () => {
                return videoRef.current ? videoRef.current.currentTime * 1000 : 0;
            }
        }));

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

        // 하이라이트 모드 처리
        useEffect(() => {
            if (!videoRef.current || !highlightMode || highlights.length === 0) return;

            const video = videoRef.current;

            const handleTimeUpdate = () => {
                const currentTimeMs = video.currentTime * 1000;

                // 현재 하이라이트 구간
                const currentHighlight = highlights[currentHighlightIndexRef.current];

                if (!currentHighlight) return;

                // 현재 하이라이트 구간을 벗어났으면 다음 하이라이트로 이동
                if (currentTimeMs >= currentHighlight.endTimeMs) {
                    currentHighlightIndexRef.current += 1;

                    // 다음 하이라이트가 있으면 이동
                    if (currentHighlightIndexRef.current < highlights.length) {
                        const nextHighlight = highlights[currentHighlightIndexRef.current];
                        video.currentTime = nextHighlight.startTimeMs / 1000;
                    } else {
                        // 모든 하이라이트를 다 봤으면 처음으로
                        currentHighlightIndexRef.current = 0;
                        video.currentTime = highlights[0].startTimeMs / 1000;
                        video.pause();
                    }
                }
                // 현재 하이라이트 구간보다 앞에 있으면 하이라이트 시작으로 이동
                else if (currentTimeMs < currentHighlight.startTimeMs) {
                    video.currentTime = currentHighlight.startTimeMs / 1000;
                }
            };

            video.addEventListener('timeupdate', handleTimeUpdate);

            return () => {
                video.removeEventListener('timeupdate', handleTimeUpdate);
            };
        }, [highlightMode, highlights]);

        // 하이라이트 모드 시작 시 첫 번째 하이라이트로 이동
        useEffect(() => {
            if (highlightMode && highlights.length > 0 && videoRef.current) {
                currentHighlightIndexRef.current = 0;
                videoRef.current.currentTime = highlights[0].startTimeMs / 1000;
            }
        }, [highlightMode, highlights]);

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
                {highlightMode && highlights.length > 0 && (
                    <div className="absolute top-4 left-4 bg-orange-500 text-white px-3 py-1 rounded-md text-sm font-semibold">
                        하이라이트 모드 ({currentHighlightIndexRef.current + 1}/{highlights.length})
                    </div>
                )}
            </div>
        );
    }
);

VideoPlayer.displayName = 'VideoPlayer';