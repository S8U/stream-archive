'use client';

import { useEffect, useRef, forwardRef, useImperativeHandle } from 'react';
import Hls from 'hls.js';

interface VideoPlayerProps {
    playlistUrl: string;
    onTimeUpdate?: (currentTimeMs: number) => void;
    initialPosition?: number | null;  // 초 단위
}

/**
 * VideoPlayer에서 외부로 노출하는 메서드
 */
export interface VideoPlayerHandle {
    seek: (timeMs: number) => void;
    play: () => void;
    pause: () => void;
    getCurrentTime: () => number;
}

export const VideoPlayer = forwardRef<VideoPlayerHandle, VideoPlayerProps>(
    function VideoPlayer({ playlistUrl, onTimeUpdate, initialPosition }, ref) {
        const videoRef = useRef<HTMLVideoElement>(null);
        const hlsRef = useRef<Hls | null>(null);
        const hasRestoredPositionRef = useRef(false);

        // 외부에서 호출 가능한 메서드 노출
        useImperativeHandle(ref, () => ({
            seek: (timeMs: number) => {
                if (videoRef.current) {
                    videoRef.current.currentTime = timeMs / 1000;
                }
            },
            play: () => {
                videoRef.current?.play();
            },
            pause: () => {
                videoRef.current?.pause();
            },
            getCurrentTime: () => {
                return (videoRef.current?.currentTime ?? 0) * 1000;
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

        // 초기 위치로 이동 (이어보기)
        useEffect(() => {
            if (!videoRef.current || !initialPosition || hasRestoredPositionRef.current) return;

            const video = videoRef.current;
            const handleCanPlay = () => {
                if (!hasRestoredPositionRef.current && initialPosition > 0) {
                    video.currentTime = initialPosition;
                    hasRestoredPositionRef.current = true;
                }
            };

            video.addEventListener('canplay', handleCanPlay);
            return () => video.removeEventListener('canplay', handleCanPlay);
        }, [initialPosition]);

        return (
            <div className="relative aspect-video bg-black overflow-hidden">
                <video
                    ref={videoRef}
                    controls
                    className="w-full h-full"
                    playsInline
                    webkit-playsinline
                    onTimeUpdate={(e) => {
                        if (onTimeUpdate) {
                            onTimeUpdate(e.currentTarget.currentTime * 1000);
                        }
                    }}
                />
            </div>
        );
    }
);
