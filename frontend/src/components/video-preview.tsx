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
}

export function VideoPreview({ thumbnailUrl, playlistUrl, title, isHovered }: VideoPreviewProps) {
    const videoRef = useRef<HTMLVideoElement>(null);
    const hlsRef = useRef<Hls | null>(null);
    const [isVideoLoaded, setIsVideoLoaded] = useState(false);
    const [imageError, setImageError] = useState(false);

    useEffect(() => {
        if (!isHovered || !videoRef.current) {
            return;
        }

        const video = videoRef.current;

        // Safari 네이티브 HLS 지원 체크
        if (video.canPlayType('application/vnd.apple.mpegurl')) {
            video.src = playlistUrl;
            video.play().then(() => {
                setIsVideoLoaded(true);
            }).catch(err => {
                console.error('Video playback failed:', err);
            });
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

            hls.on(Hls.Events.MANIFEST_PARSED, () => {
                video.play().then(() => {
                    setIsVideoLoaded(true);
                }).catch(err => {
                    console.error('Video playback failed:', err);
                });
            });

            hls.on(Hls.Events.ERROR, (_event, data) => {
                if (data.fatal) {
                    console.error('HLS fatal error:', data);
                }
            });
        } else {
            console.error('HLS is not supported in this browser');
        }

        return () => {
            // Cleanup
            if (hlsRef.current) {
                hlsRef.current.destroy();
                hlsRef.current = null;
            }
            setIsVideoLoaded(false);
        };
    }, [isHovered, playlistUrl]);

    return (
        <div className="relative w-full h-full">
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