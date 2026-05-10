'use client';

import { useEffect, useRef, useState, useCallback, useMemo, MouseEvent as ReactMouseEvent } from 'react';
import Hls from 'hls.js';
import {
    Play,
    Pause,
    Volume2,
    VolumeX,
    Maximize,
    Minimize,
    PanelRight,
    Loader2,
    ChevronLeft,
    ChevronRight,
    PictureInPicture2,
} from 'lucide-react';

interface ViewerPoint {
    offsetMillis: number;
    viewerCount: number;
}

interface VideoPlayerProps {
    playlistUrl: string;
    onTimeUpdate?: (currentTimeMs: number) => void;
    initialPosition?: number | null; // 초 단위
    isLive?: boolean;
    isWide?: boolean;
    onWideToggle?: (isWide: boolean) => void;
    viewerHistory?: ViewerPoint[];
}

const HIDE_DELAY_MS = 3000;
const SEEK_STEP_SEC = 5;
const SEEK_STEP_LONG_SEC = 10;
const VOLUME_STEP = 0.05;
const PLAY_PAUSE_INDICATOR_MS = 1000;
const SEEK_INDICATOR_MS = 1000;
const VOLUME_STORAGE_KEY = 'video-player:volume';
const MUTED_STORAGE_KEY = 'video-player:muted';

function formatTime(seconds: number): string {
    if (!isFinite(seconds) || seconds < 0) return '00:00';
    const total = Math.floor(seconds);
    const h = Math.floor(total / 3600);
    const m = Math.floor((total % 3600) / 60);
    const s = total % 60;
    if (h > 0) {
        return `${h}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`;
    }
    return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`;
}

interface ControlButtonProps {
    onClick: () => void;
    label: string;
    shortcut?: string;
    align?: 'left' | 'center' | 'right';
    onHoverEnter?: () => void;
    onHoverLeave?: () => void;
    children: React.ReactNode;
}

function ControlButton({ onClick, label, shortcut, align = 'center', onHoverEnter, onHoverLeave, children }: ControlButtonProps) {
    const tooltipAlign =
        align === 'left'
            ? 'left-0'
            : align === 'right'
                ? 'right-0'
                : 'left-1/2 -translate-x-1/2';
    return (
        <div
            className="relative group/btn"
            onMouseEnter={onHoverEnter}
            onMouseLeave={onHoverLeave}
        >
            <button
                type="button"
                onClick={onClick}
                className="p-1 transition-colors cursor-pointer text-white/80 hover:text-white"
                aria-label={label}
            >
                {children}
            </button>
            <div className={`absolute bottom-full ${tooltipAlign} mb-3 px-3 py-1.5 bg-black/50 text-white text-xs rounded-full whitespace-nowrap pointer-events-none opacity-0 group-hover/btn:opacity-100 transition-opacity z-20`}>
                {label}
                {shortcut && <span> ({shortcut})</span>}
            </div>
        </div>
    );
}

export function VideoPlayer({
    playlistUrl,
    onTimeUpdate,
    initialPosition,
    isLive = false,
    isWide: isWideProp,
    onWideToggle,
    viewerHistory,
}: VideoPlayerProps) {
    const videoRef = useRef<HTMLVideoElement>(null);
    const containerRef = useRef<HTMLDivElement>(null);
    const controlsRef = useRef<HTMLDivElement>(null);
    const timelineRef = useRef<HTMLDivElement>(null);
    const hlsRef = useRef<Hls | null>(null);
    const hasRestoredPositionRef = useRef(false);
    const hideTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
    const isHoveringControlsRef = useRef(false);
    const previousVolumeRef = useRef(1);

    const [isPlaying, setIsPlaying] = useState(false);
    const [currentTime, setCurrentTime] = useState(0);
    const [duration, setDuration] = useState(0);
    const [volume, setVolume] = useState(1);
    const [isMuted, setIsMuted] = useState(false);
    const [isFullscreen, setIsFullscreen] = useState(false);
    const [isWideInternal, setIsWideInternal] = useState(false);
    const isWide = isWideProp ?? isWideInternal;
    const [isControlsVisible, setIsControlsVisible] = useState(true);
    const [isVolumeHover, setIsVolumeHover] = useState(false);
    const [isDraggingTimeline, setIsDraggingTimeline] = useState(false);
    const [hoverTime, setHoverTime] = useState<number | null>(null);
    const [hoverX, setHoverX] = useState(0);
    const [isBuffering, setIsBuffering] = useState(true);
    const [volumeIndicator, setVolumeIndicator] = useState<{ value: number; nonce: number } | null>(null);
    const volumeIndicatorTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

    const [isPipSupported, setIsPipSupported] = useState(false);
    const [isPip, setIsPip] = useState(false);
    const isUserActionRef = useRef(false); // 사용자가 직접 재생/일시정지한 경우에만 true
    const clickTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

    const [playPauseIndicator, setPlayPauseIndicator] = useState<{ kind: 'play' | 'pause'; nonce: number } | null>(null);
    const playPauseIndicatorTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

    const [seekIndicator, setSeekIndicator] = useState<{ direction: 'backward' | 'forward'; seconds: number } | null>(null);
    const seekIndicatorTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

    useEffect(() => {
        setIsPipSupported(!!document.pictureInPictureEnabled);
    }, []);

    // HLS 로드
    useEffect(() => {
        if (!videoRef.current) return;
        const video = videoRef.current;

        // 저장된 볼륨/음소거 복원
        try {
            const savedVolume = localStorage.getItem(VOLUME_STORAGE_KEY);
            const savedMuted = localStorage.getItem(MUTED_STORAGE_KEY);
            if (savedVolume !== null) {
                const v = parseFloat(savedVolume);
                if (!isNaN(v) && v >= 0 && v <= 1) {
                    video.volume = v;
                    previousVolumeRef.current = v > 0 ? v : 1;
                }
            }
            if (savedMuted === 'true') {
                video.muted = true;
            }
        } catch {
            // localStorage 접근 실패 무시
        }

        if (video.canPlayType('application/vnd.apple.mpegurl')) {
            video.src = playlistUrl;
        } else if (Hls.isSupported()) {
            const hls = new Hls({ enableWorker: true, lowLatencyMode: false });
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

        // 자동재생 시도 (브라우저 정책에 의해 거부되면 사용자 클릭 대기)
        const tryAutoplay = () => {
            video.play().catch(() => {
                // 자동재생 실패 시 사용자 클릭 대기
            });
        };
        video.addEventListener('canplay', tryAutoplay, { once: true });

        return () => {
            video.removeEventListener('canplay', tryAutoplay);
            if (hlsRef.current) {
                hlsRef.current.destroy();
                hlsRef.current = null;
            }
        };
    }, [playlistUrl]);

    // 초기 위치 복원
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

    // 풀스크린 변경 감지
    useEffect(() => {
        const handler = () => {
            setIsFullscreen(!!document.fullscreenElement);
        };
        document.addEventListener('fullscreenchange', handler);
        return () => document.removeEventListener('fullscreenchange', handler);
    }, []);

    // 자동 숨김 처리
    const showControls = useCallback(() => {
        setIsControlsVisible(true);
        if (hideTimerRef.current) clearTimeout(hideTimerRef.current);

        const video = videoRef.current;
        if (!video) return;

        // 일시정지/드래깅/컨트롤 hover 중에는 숨기지 않음
        if (video.paused || isDraggingTimeline || isHoveringControlsRef.current) {
            return;
        }

        hideTimerRef.current = setTimeout(() => {
            if (!isHoveringControlsRef.current && !isDraggingTimeline) {
                const v = videoRef.current;
                if (v && !v.paused) {
                    setIsControlsVisible(false);
                }
            }
        }, HIDE_DELAY_MS);
    }, [isDraggingTimeline]);

    const hideControlsImmediately = useCallback(() => {
        if (hideTimerRef.current) clearTimeout(hideTimerRef.current);
        const video = videoRef.current;
        if (video && !video.paused && !isHoveringControlsRef.current && !isDraggingTimeline) {
            setIsControlsVisible(false);
        }
    }, [isDraggingTimeline]);

    // 재생/일시정지 토글
    const togglePlay = useCallback(() => {
        const video = videoRef.current;
        if (!video) return;
        isUserActionRef.current = true;
        if (video.paused) {
            video.play().catch((err) => console.error('Play failed:', err));
        } else {
            video.pause();
        }
    }, []);

    // 재생/일시정지 중앙 인디케이터
    const showPlayPauseIndicator = useCallback((kind: 'play' | 'pause') => {
        setPlayPauseIndicator({ kind, nonce: Date.now() });
        if (playPauseIndicatorTimerRef.current) clearTimeout(playPauseIndicatorTimerRef.current);
        playPauseIndicatorTimerRef.current = setTimeout(() => {
            setPlayPauseIndicator(null);
        }, PLAY_PAUSE_INDICATOR_MS);
    }, []);

    // 시킹 ±N초 인디케이터 (같은 방향 연속 누름 시 합산)
    const showSeekIndicator = useCallback((direction: 'backward' | 'forward', step: number) => {
        setSeekIndicator((prev) => {
            const sameDirection = prev && prev.direction === direction;
            const nextSeconds = (sameDirection ? prev!.seconds : 0) + step;
            return { direction, seconds: nextSeconds };
        });
        if (seekIndicatorTimerRef.current) clearTimeout(seekIndicatorTimerRef.current);
        seekIndicatorTimerRef.current = setTimeout(() => {
            setSeekIndicator(null);
        }, SEEK_INDICATOR_MS);
    }, []);

    // 볼륨 인디케이터 잠시 표시
    const showVolumeIndicator = useCallback((value: number) => {
        setVolumeIndicator({ value: Math.round(value * 100), nonce: Date.now() });
        if (volumeIndicatorTimerRef.current) clearTimeout(volumeIndicatorTimerRef.current);
        volumeIndicatorTimerRef.current = setTimeout(() => {
            setVolumeIndicator(null);
        }, 1000);
    }, []);

    // 음소거 토글
    const toggleMute = useCallback(() => {
        const video = videoRef.current;
        if (!video) return;
        if (video.muted || video.volume === 0) {
            video.muted = false;
            const restored = previousVolumeRef.current || 0.5;
            video.volume = restored;
            setVolume(restored);
            setIsMuted(false);
            showVolumeIndicator(restored);
            try {
                localStorage.setItem(VOLUME_STORAGE_KEY, String(restored));
                localStorage.setItem(MUTED_STORAGE_KEY, 'false');
            } catch {}
        } else {
            previousVolumeRef.current = video.volume;
            video.muted = true;
            setIsMuted(true);
            showVolumeIndicator(0);
            try {
                localStorage.setItem(MUTED_STORAGE_KEY, 'true');
            } catch {}
        }
    }, [showVolumeIndicator]);

    // 볼륨 변경
    const changeVolume = useCallback((newVolume: number) => {
        const video = videoRef.current;
        if (!video) return;
        const clamped = Math.max(0, Math.min(1, newVolume));
        video.volume = clamped;
        video.muted = clamped === 0;
        setVolume(clamped);
        setIsMuted(clamped === 0);
        if (clamped > 0) previousVolumeRef.current = clamped;
        showVolumeIndicator(clamped);
        try {
            localStorage.setItem(VOLUME_STORAGE_KEY, String(clamped));
            localStorage.setItem(MUTED_STORAGE_KEY, clamped === 0 ? 'true' : 'false');
        } catch {}
    }, [showVolumeIndicator]);

    // 시킹
    const seekTo = useCallback((time: number) => {
        const video = videoRef.current;
        if (!video) return;
        const clamped = Math.max(0, Math.min(video.duration || 0, time));
        video.currentTime = clamped;
        setCurrentTime(clamped);
    }, []);

    // 풀스크린 토글
    const toggleFullscreen = useCallback(() => {
        const container = containerRef.current;
        if (!container) return;
        if (document.fullscreenElement) {
            document.exitFullscreen().catch(() => {});
        } else {
            container.requestFullscreen().catch(() => {});
        }
    }, []);

    // PiP 토글
    const togglePip = useCallback(() => {
        const video = videoRef.current;
        if (!video) return;
        if (document.pictureInPictureElement) {
            document.exitPictureInPicture().catch(() => {});
        } else {
            video.requestPictureInPicture().catch(() => {});
        }
    }, []);

    // PiP 상태 동기화
    useEffect(() => {
        const video = videoRef.current;
        if (!video) return;
        const onEnter = () => setIsPip(true);
        const onLeave = () => setIsPip(false);
        video.addEventListener('enterpictureinpicture', onEnter);
        video.addEventListener('leavepictureinpicture', onLeave);
        return () => {
            video.removeEventListener('enterpictureinpicture', onEnter);
            video.removeEventListener('leavepictureinpicture', onLeave);
        };
    }, []);

    // 와이드 토글
    const toggleWide = useCallback(() => {
        const next = !isWide;
        if (isWideProp === undefined) {
            setIsWideInternal(next);
        }
        if (onWideToggle) onWideToggle(next);
    }, [isWide, isWideProp, onWideToggle]);

    // 비디오 이벤트
    useEffect(() => {
        const video = videoRef.current;
        if (!video) return;

        const handlePlay = () => {
            setIsPlaying(true);
            showControls();
            if (isUserActionRef.current) {
                showPlayPauseIndicator('play');
                isUserActionRef.current = false;
            }
        };
        const handlePause = () => {
            setIsPlaying(false);
            if (isUserActionRef.current) {
                showPlayPauseIndicator('pause');
                isUserActionRef.current = false;
            }
        };
        const handleTimeUpdate = () => {
            setCurrentTime(video.currentTime);
            if (onTimeUpdate) onTimeUpdate(video.currentTime * 1000);
        };
        const handleDurationChange = () => setDuration(video.duration || 0);
        const handleVolumeChange = () => {
            setVolume(video.volume);
            setIsMuted(video.muted);
        };
        const handleLoadedMetadata = () => {
            setDuration(video.duration || 0);
            setVolume(video.volume);
            setIsMuted(video.muted);
        };
        const handleWaiting = () => setIsBuffering(true);
        const handlePlaying = () => setIsBuffering(false);
        const handleCanPlay = () => setIsBuffering(false);

        video.addEventListener('play', handlePlay);
        video.addEventListener('pause', handlePause);
        video.addEventListener('timeupdate', handleTimeUpdate);
        video.addEventListener('durationchange', handleDurationChange);
        video.addEventListener('volumechange', handleVolumeChange);
        video.addEventListener('loadedmetadata', handleLoadedMetadata);
        video.addEventListener('waiting', handleWaiting);
        video.addEventListener('playing', handlePlaying);
        video.addEventListener('canplay', handleCanPlay);

        return () => {
            video.removeEventListener('play', handlePlay);
            video.removeEventListener('pause', handlePause);
            video.removeEventListener('timeupdate', handleTimeUpdate);
            video.removeEventListener('durationchange', handleDurationChange);
            video.removeEventListener('volumechange', handleVolumeChange);
            video.removeEventListener('loadedmetadata', handleLoadedMetadata);
            video.removeEventListener('waiting', handleWaiting);
            video.removeEventListener('playing', handlePlaying);
            video.removeEventListener('canplay', handleCanPlay);
        };
    }, [onTimeUpdate, showControls, showPlayPauseIndicator]);

    // 키보드 단축키 (e.code 기반 → 한글 IME 상태에서도 동작)
    useEffect(() => {
        const handleKeyDown = (e: KeyboardEvent) => {
            const target = e.target as HTMLElement;
            // input, textarea, contentEditable에서는 동작 안 함
            if (
                target.tagName === 'INPUT' ||
                target.tagName === 'TEXTAREA' ||
                target.isContentEditable
            ) {
                return;
            }

            const video = videoRef.current;
            if (!video) return;

            switch (e.code) {
                case 'Space':
                case 'KeyK':
                    e.preventDefault();
                    togglePlay();
                    showControls();
                    break;
                case 'ArrowLeft':
                    e.preventDefault();
                    seekTo(video.currentTime - SEEK_STEP_SEC);
                    showControls();
                    showSeekIndicator('backward', SEEK_STEP_SEC);
                    break;
                case 'ArrowRight':
                    e.preventDefault();
                    seekTo(video.currentTime + SEEK_STEP_SEC);
                    showControls();
                    showSeekIndicator('forward', SEEK_STEP_SEC);
                    break;
                case 'KeyJ':
                    e.preventDefault();
                    seekTo(video.currentTime - SEEK_STEP_LONG_SEC);
                    showControls();
                    showSeekIndicator('backward', SEEK_STEP_LONG_SEC);
                    break;
                case 'KeyL':
                    e.preventDefault();
                    seekTo(video.currentTime + SEEK_STEP_LONG_SEC);
                    showControls();
                    showSeekIndicator('forward', SEEK_STEP_LONG_SEC);
                    break;
                case 'ArrowUp':
                    e.preventDefault();
                    changeVolume(Math.round((video.volume + VOLUME_STEP) / VOLUME_STEP) * VOLUME_STEP);
                    showControls();
                    break;
                case 'ArrowDown':
                    e.preventDefault();
                    changeVolume(Math.round((video.volume - VOLUME_STEP) / VOLUME_STEP) * VOLUME_STEP);
                    showControls();
                    break;
                case 'KeyM':
                    e.preventDefault();
                    toggleMute();
                    showControls();
                    break;
                case 'KeyF':
                    e.preventDefault();
                    toggleFullscreen();
                    break;
                case 'KeyT':
                    e.preventDefault();
                    toggleWide();
                    break;
                case 'Digit0':
                case 'Digit1':
                case 'Digit2':
                case 'Digit3':
                case 'Digit4':
                case 'Digit5':
                case 'Digit6':
                case 'Digit7':
                case 'Digit8':
                case 'Digit9': {
                    e.preventDefault();
                    if (video.duration > 0) {
                        const digit = parseInt(e.code.slice(-1), 10);
                        seekTo(video.duration * (digit / 10));
                        showControls();
                    }
                    break;
                }
            }
        };

        window.addEventListener('keydown', handleKeyDown);
        return () => window.removeEventListener('keydown', handleKeyDown);
    }, [togglePlay, seekTo, changeVolume, toggleMute, toggleFullscreen, toggleWide, showControls, showSeekIndicator]);

    // 타임라인 좌표 -> 시간 변환
    const timelineXToTime = useCallback((clientX: number): number => {
        const timeline = timelineRef.current;
        if (!timeline || !duration) return 0;
        const rect = timeline.getBoundingClientRect();
        const ratio = Math.max(0, Math.min(1, (clientX - rect.left) / rect.width));
        return ratio * duration;
    }, [duration]);

    // 타임라인 마우스 다운 (드래그 시작)
    const handleTimelineMouseDown = useCallback((e: ReactMouseEvent<HTMLDivElement>) => {
        e.stopPropagation();
        if (!duration) return;
        setIsDraggingTimeline(true);
        seekTo(timelineXToTime(e.clientX));
    }, [duration, seekTo, timelineXToTime]);

    // 드래그 중 마우스 이동/업
    useEffect(() => {
        if (!isDraggingTimeline) return;

        const handleMove = (e: MouseEvent) => {
            seekTo(timelineXToTime(e.clientX));
        };
        const handleUp = () => {
            setIsDraggingTimeline(false);
        };

        window.addEventListener('mousemove', handleMove);
        window.addEventListener('mouseup', handleUp);
        return () => {
            window.removeEventListener('mousemove', handleMove);
            window.removeEventListener('mouseup', handleUp);
        };
    }, [isDraggingTimeline, seekTo, timelineXToTime]);

    // 타임라인 hover
    const handleTimelineMouseMove = useCallback((e: ReactMouseEvent<HTMLDivElement>) => {
        const timeline = timelineRef.current;
        if (!timeline || !duration) return;
        const rect = timeline.getBoundingClientRect();
        const x = e.clientX - rect.left;
        setHoverX(x);
        setHoverTime(timelineXToTime(e.clientX));
    }, [duration, timelineXToTime]);

    const handleTimelineMouseLeave = useCallback(() => {
        setHoverTime(null);
    }, []);

    // 컨테이너 마우스 인터랙션
    const handleContainerMouseMove = useCallback(() => {
        showControls();
    }, [showControls]);

    const handleContainerMouseLeave = useCallback(() => {
        hideControlsImmediately();
    }, [hideControlsImmediately]);

    // 컨테이너 클릭 (재생/일시정지) — 더블클릭과 구분하기 위해 딜레이 후 실행
    const handleContainerClick = useCallback((e: ReactMouseEvent<HTMLDivElement>) => {
        const controls = controlsRef.current;
        if (controls && controls.contains(e.target as Node)) return;
        if (clickTimerRef.current) clearTimeout(clickTimerRef.current);
        clickTimerRef.current = setTimeout(() => {
            togglePlay();
        }, 200);
    }, [togglePlay]);

    // 더블클릭 (풀스크린) — 싱글클릭 타이머를 취소하고 풀스크린 실행
    const handleContainerDoubleClick = useCallback((e: ReactMouseEvent<HTMLDivElement>) => {
        const controls = controlsRef.current;
        if (controls && controls.contains(e.target as Node)) return;
        if (clickTimerRef.current) {
            clearTimeout(clickTimerRef.current);
            clickTimerRef.current = null;
        }
        toggleFullscreen();
    }, [toggleFullscreen]);

    // 컨트롤 hover 상태 추적
    const handleControlsEnter = useCallback(() => {
        isHoveringControlsRef.current = true;
        if (hideTimerRef.current) clearTimeout(hideTimerRef.current);
        setIsControlsVisible(true);
    }, []);

    const handleControlsLeave = useCallback(() => {
        isHoveringControlsRef.current = false;
        showControls();
    }, [showControls]);

    const progressPercent = duration > 0 ? (currentTime / duration) * 100 : 0;
    const hoverPercent = duration > 0 && hoverTime !== null ? (hoverTime / duration) * 100 : 0;
    const displayVolume = isMuted ? 0 : volume;

    // hover 시점 근처 시청자 수 (가장 가까운 포인트)
    const hoverViewerCount = useMemo(() => {
        if (hoverTime === null || !viewerHistory || viewerHistory.length === 0) return null;
        const targetMs = hoverTime * 1000;
        let closest = viewerHistory[0];
        let minDiff = Math.abs(closest.offsetMillis - targetMs);
        for (let i = 1; i < viewerHistory.length; i++) {
            const diff = Math.abs(viewerHistory[i].offsetMillis - targetMs);
            if (diff < minDiff) {
                minDiff = diff;
                closest = viewerHistory[i];
            }
        }
        return closest.viewerCount;
    }, [hoverTime, viewerHistory]);

    // 타임라인 hover 툴팁 X 좌표 clamp
    const tooltipX = useMemo(() => {
        const timeline = timelineRef.current;
        if (!timeline) return hoverX;
        const width = timeline.getBoundingClientRect().width;
        // 툴팁 절반 너비 약 32px 가정, 가장자리에서 잘림 방지
        const half = 32;
        return Math.max(half, Math.min(width - half, hoverX));
    }, [hoverX]);

    // 시청자 그래프 path (viewBox 100 x 100, 위쪽이 높은 시청자)
    const viewerGraphPath = useMemo(() => {
        if (!viewerHistory || viewerHistory.length < 2 || duration <= 0) return null;
        const durationMs = duration * 1000;
        const maxCount = viewerHistory.reduce((m, p) => Math.max(m, p.viewerCount), 0);
        if (maxCount <= 0) return null;

        const points = viewerHistory
            .filter((p) => p.offsetMillis >= 0 && p.offsetMillis <= durationMs)
            .map((p) => {
                const x = (p.offsetMillis / durationMs) * 100;
                const y = 100 - (p.viewerCount / maxCount) * 100;
                return `${x.toFixed(2)},${y.toFixed(2)}`;
            });

        if (points.length < 2) return null;

        // 첫/끝 포인트를 각각 x=0, x=100 끝까지 수평으로 연장 후 아래쪽으로 닫아 채우기
        const firstY = points[0].split(',')[1];
        const lastY = points[points.length - 1].split(',')[1];
        return `M0,${firstY} L${points.join(' L')} L100,${lastY} L100,100 L0,100 Z`;
    }, [viewerHistory, duration]);

    return (
        <div
            ref={containerRef}
            className={`relative bg-black group ${
                isFullscreen
                    ? 'w-screen h-screen overflow-hidden'
                    : isWide
                        ? 'w-full h-screen overflow-hidden'
                        : 'w-full aspect-video'
            }`}
            onMouseMove={handleContainerMouseMove}
            onMouseLeave={handleContainerMouseLeave}
            onClick={handleContainerClick}
            onDoubleClick={handleContainerDoubleClick}
            style={{ cursor: isControlsVisible ? 'default' : 'none' }}
        >
            <video
                ref={videoRef}
                className="w-full h-full object-contain"
                playsInline
                webkit-playsinline=""
            />

            {/* 버퍼링/로딩 스피너 */}
            {isBuffering && (
                <div className="absolute inset-0 flex items-center justify-center pointer-events-none">
                    <Loader2 className="w-12 h-12 text-white/80 animate-spin" />
                </div>
            )}

            {/* 볼륨 인디케이터 */}
            {volumeIndicator !== null && (
                <div
                    key={`vol-${volumeIndicator.nonce}`}
                    className="absolute top-6 left-1/2 -translate-x-1/2 pointer-events-none"
                    style={{ animation: `videoPlayerFadeOut 1000ms ease-out forwards` }}
                >
                    <div className="px-4 py-2 bg-black/50 text-white/80 rounded-full text-sm font-medium">
                        {volumeIndicator.value}%
                    </div>
                </div>
            )}

            {/* 재생/일시정지 중앙 인디케이터 */}
            {playPauseIndicator !== null && (
                <div
                    key={`pp-${playPauseIndicator.kind}-${playPauseIndicator.nonce}`}
                    className="absolute inset-0 flex items-center justify-center pointer-events-none"
                    style={{ animation: `videoPlayerFadeOut ${PLAY_PAUSE_INDICATOR_MS}ms ease-out forwards` }}
                >
                    <div className="bg-black/50 rounded-full p-5">
                        {playPauseIndicator.kind === 'play' ? (
                            <Play size={48} className="text-white" fill="currentColor" />
                        ) : (
                            <Pause size={48} className="text-white" fill="currentColor" />
                        )}
                    </div>
                </div>
            )}

            {/* 시킹 ±N초 인디케이터 */}
            {seekIndicator !== null && (
                <div
                    key={`seek-${seekIndicator.direction}-${seekIndicator.seconds}`}
                    className={`absolute top-1/2 -translate-y-1/2 pointer-events-none ${
                        seekIndicator.direction === 'backward' ? 'left-[5%]' : 'right-[5%]'
                    }`}
                    style={{ animation: `videoPlayerFadeOut ${SEEK_INDICATOR_MS}ms ease-out forwards` }}
                >
                    <div className="flex items-center gap-4 text-white font-bold text-2xl drop-shadow-[0_1px_3px_rgba(0,0,0,0.8)]">
                        {seekIndicator.direction === 'backward' ? (
                            <>
                                <ChevronLeft size={28} strokeWidth={3} />
                                <span className="tabular-nums">-{seekIndicator.seconds}</span>
                            </>
                        ) : (
                            <>
                                <span className="tabular-nums">+{seekIndicator.seconds}</span>
                                <ChevronRight size={28} strokeWidth={3} />
                            </>
                        )}
                    </div>
                </div>
            )}

            {/* 컨트롤 오버레이 */}
            <div
                ref={controlsRef}
                className={`absolute inset-x-0 bottom-0 z-10 transition-opacity duration-200 ${
                    isControlsVisible ? 'opacity-100' : 'opacity-0 pointer-events-none'
                }`}
                onClick={(e) => e.stopPropagation()}
                onDoubleClick={(e) => e.stopPropagation()}
            >
                {/* 그라디언트 배경 (hover 판정에서 제외) */}
                <div className="absolute inset-x-0 bottom-0 h-32 bg-gradient-to-t from-black/80 to-transparent pointer-events-none" />

                {/* 타임라인 */}
                <div
                    className="relative px-4 pt-2"
                    onMouseEnter={handleControlsEnter}
                    onMouseLeave={handleControlsLeave}
                >
                    <div
                        ref={timelineRef}
                        className="relative h-1.5 bg-white/20 rounded-full cursor-pointer group/timeline hover:bg-white/40 transition-colors"
                        onMouseDown={handleTimelineMouseDown}
                        onMouseMove={handleTimelineMouseMove}
                        onMouseLeave={handleTimelineMouseLeave}
                    >
                        {/* 시청자 수 그래프 (hover 시 표시) */}
                        {viewerGraphPath && (
                            <svg
                                className="absolute -top-10 left-0 w-full h-10 pointer-events-none overflow-visible opacity-0 group-hover/timeline:opacity-100 transition-opacity duration-200"
                                viewBox="0 0 100 100"
                                preserveAspectRatio="none"
                                aria-hidden="true"
                            >
                                <path
                                    d={viewerGraphPath}
                                    fill="rgba(255, 255, 255, 0.2)"
                                    stroke="none"
                                />
                            </svg>
                        )}

                        {/* hover 위치 표시 */}
                        {hoverTime !== null && (
                            <div
                                className="absolute top-0 h-full bg-white/30 rounded-full pointer-events-none"
                                style={{ width: `${hoverPercent}%` }}
                            />
                        )}

                        {/* 진행률 */}
                        <div
                            className="absolute top-0 left-0 h-full bg-white rounded-full pointer-events-none"
                            style={{ width: `${progressPercent}%` }}
                        />

                        {/* 썸 (재생 핸들) */}
                        <div
                            className="absolute top-1/2 -translate-y-1/2 -translate-x-1/2 w-3.5 h-3.5 bg-white rounded-full opacity-0 group-hover/timeline:opacity-100 transition-opacity pointer-events-none"
                            style={{ left: `${progressPercent}%` }}
                        />

                        {/* hover 시간/시청자 툴팁 */}
                        {hoverTime !== null && (
                            <div
                                className="absolute bottom-full mb-6 px-3 py-1.5 bg-black/50 text-white text-xs rounded-full pointer-events-none whitespace-nowrap text-center"
                                style={{
                                    left: `${tooltipX}px`,
                                    transform: 'translateX(-50%)',
                                }}
                            >
                                <div className="font-bold">{formatTime(hoverTime)}</div>
                                {hoverViewerCount !== null && (
                                    <div className="text-white/70">
                                        시청자 {hoverViewerCount.toLocaleString()}
                                    </div>
                                )}
                            </div>
                        )}
                    </div>
                </div>

                {/* 컨트롤 바 */}
                <div className="relative px-4 py-3 flex items-center gap-3 text-white">
                    {/* 재생/일시정지 */}
                    <ControlButton
                        onClick={togglePlay}
                        label={isPlaying ? '일시정지' : '재생'}
                        shortcut="space"
                        align="left"
                        onHoverEnter={handleControlsEnter}
                        onHoverLeave={handleControlsLeave}
                    >
                        {isPlaying ? <Pause size={20} fill="currentColor" /> : <Play size={20} fill="currentColor" />}
                    </ControlButton>

                    {/* 볼륨 */}
                    <div
                        className="flex items-center gap-2"
                        onMouseEnter={() => {
                            setIsVolumeHover(true);
                            handleControlsEnter();
                        }}
                        onMouseLeave={() => {
                            setIsVolumeHover(false);
                            handleControlsLeave();
                        }}
                    >
                        <ControlButton
                            onClick={toggleMute}
                            label={isMuted ? '음소거 해제' : '음소거'}
                            shortcut="M"
                        >
                            {isMuted || volume === 0 ? <VolumeX size={20} /> : <Volume2 size={20} />}
                        </ControlButton>
                        <div
                            className={`overflow-hidden transition-all duration-200 ${
                                isVolumeHover ? 'w-20' : 'w-0'
                            }`}
                        >
                            <input
                                type="range"
                                min={0}
                                max={1}
                                step={0.01}
                                value={displayVolume}
                                onChange={(e) => changeVolume(parseFloat(e.target.value))}
                                className="w-20 h-1 accent-white cursor-pointer align-middle"
                                aria-label="볼륨"
                            />
                        </div>
                    </div>

                    {/* 시간 표시 */}
                    <div
                        className="text-sm tabular-nums flex items-center gap-2"
                        onMouseEnter={handleControlsEnter}
                        onMouseLeave={handleControlsLeave}
                    >
                        {isLive ? (
                            <>
                                <span className="flex items-center gap-1.5">
                                    <span className="inline-block w-2 h-2 bg-red-500 rounded-full animate-pulse" />
                                    <span className="font-semibold">LIVE</span>
                                </span>
                            </>
                        ) : (
                            <span>
                                {formatTime(currentTime)} / {formatTime(duration)}
                            </span>
                        )}
                    </div>

                    <div className="flex-1" />

                    {/* PiP */}
                    {isPipSupported && (
                        <ControlButton
                            onClick={togglePip}
                            label={isPip ? 'PIP 종료' : 'PIP 보기'}
                            onHoverEnter={handleControlsEnter}
                            onHoverLeave={handleControlsLeave}
                        >
                            <PictureInPicture2 size={20} />
                        </ControlButton>
                    )}

                    {/* 와이드스크린 */}
                    <ControlButton
                        onClick={toggleWide}
                        label={isWide ? '좁은 화면' : '넓은 화면'}
                        shortcut="T"
                        onHoverEnter={handleControlsEnter}
                        onHoverLeave={handleControlsLeave}
                    >
                        <PanelRight size={20} />
                    </ControlButton>

                    {/* 풀스크린 */}
                    <ControlButton
                        onClick={toggleFullscreen}
                        label={isFullscreen ? '전체화면 해제' : '전체화면'}
                        shortcut="F"
                        align="right"
                        onHoverEnter={handleControlsEnter}
                        onHoverLeave={handleControlsLeave}
                    >
                        {isFullscreen ? <Minimize size={20} /> : <Maximize size={20} />}
                    </ControlButton>
                </div>
            </div>
        </div>
    );
}
