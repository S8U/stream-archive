'use client';

import { useEffect, useRef } from 'react';
import Hls from 'hls.js';

interface VideoPlayerProps {
    playlistUrl: string;
}

export function VideoPlayer({ playlistUrl }: VideoPlayerProps) {
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

    return (
        <div className="relative aspect-video bg-black overflow-hidden">
            <video
                ref={videoRef}
                controls
                className="w-full h-full"
            />
        </div>
    );
}