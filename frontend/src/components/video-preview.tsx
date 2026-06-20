'use client';

import {useEffect, useRef, useState} from 'react';
import Hls from 'hls.js';
import Image from 'next/image';
import {VideoOff} from 'lucide-react';

interface VideoPreviewProps {
    thumbnailUrl: string;
    playlistUrl: string;
    title: string;
    isHovered: boolean;
    isLive?: boolean;
}

function isInterruptedPlayback(error: unknown): boolean {
    return error instanceof DOMException && error.name === 'AbortError';
}

export function VideoPreview({ thumbnailUrl, playlistUrl, title, isHovered, isLive }: VideoPreviewProps) {
    const videoRef = useRef<HTMLVideoElement>(null);
    const hlsRef = useRef<Hls | null>(null);
    const [isVideoLoaded, setIsVideoLoaded] = useState(false);
    const [imageError, setImageError] = useState(false);
    // 녹화 중에는 썸네일이 주기적으로 갱신되므로 10초마다 캐시버스터 값을 바꿔 새로 받는다
    const [cacheBuster, setCacheBuster] = useState(0);

    useEffect(() => {
        if (!isLive) {
            return;
        }

        setCacheBuster(Math.floor(Date.now() / 10000));
        const timer = setInterval(() => {
            setCacheBuster(Math.floor(Date.now() / 10000));
        }, 10000);

        return () => clearInterval(timer);
    }, [isLive]);

    useEffect(() => {
        if (!isHovered || !videoRef.current) {
            return;
        }

        const video = videoRef.current;
        let cancelled = false;

        const markVideoLoaded = () => {
            if (!cancelled) {
                setIsVideoLoaded(true);
            }
        };

        const handlePlaybackError = (error: unknown) => {
            if (!cancelled && !isInterruptedPlayback(error)) {
                console.error('Video playback failed:', error);
            }
        };

        if (Hls.isSupported()) {
            const hls = new Hls({
                enableWorker: true,
                lowLatencyMode: false,
            });

            hlsRef.current = hls;

            hls.loadSource(playlistUrl);
            hls.attachMedia(video);

            hls.on(Hls.Events.MANIFEST_PARSED, () => {
                video.play().then(markVideoLoaded).catch(handlePlaybackError);
            });

            hls.on(Hls.Events.ERROR, (_event, data) => {
                if (data.fatal) {
                    console.error('HLS fatal error:', data);
                }
            });
        } else if (video.canPlayType('application/vnd.apple.mpegurl')) {
            video.src = playlistUrl;
            video.play().then(markVideoLoaded).catch(handlePlaybackError);
        } else {
            console.error('HLS is not supported in this browser');
        }

        return () => {
            // Cleanup
            cancelled = true;
            if (hlsRef.current) {
                hlsRef.current.destroy();
                hlsRef.current = null;
            }
            setIsVideoLoaded(false);
        };
    }, [isHovered, playlistUrl]);

    return (
        <div className="relative w-full h-full pointer-events-none">
            {/* 비디오 미리보기 */}
            {isHovered && (
                <video
                    ref={videoRef}
                    muted
                    loop
                    playsInline
                    className={`absolute inset-0 w-full h-full object-cover transition-opacity duration-300 z-10 ${isVideoLoaded ? 'opacity-100' : 'opacity-0'}`}
                />
            )}

            {/* 썸네일 */}
            <div className="absolute inset-0">
                {imageError ? (
                    <div className="w-full h-full flex items-center justify-center bg-muted">
                        <VideoOff className="w-16 h-16 text-muted-foreground" />
                    </div>
                ) : isLive ? (
                    // 녹화 중에는 next/image 최적화 캐시를 우회하기 위해 일반 img를 쓴다
                    // eslint-disable-next-line @next/next/no-img-element
                    <img
                        src={`${thumbnailUrl}?t=${cacheBuster}`}
                        alt={title}
                        className="w-full h-full object-cover"
                        onError={() => setImageError(true)}
                    />
                ) : (
                    <Image
                        src={thumbnailUrl}
                        alt={title}
                        fill
                        className="object-cover"
                        onError={() => setImageError(true)}
                    />
                )}
            </div>
        </div>
    );
}
