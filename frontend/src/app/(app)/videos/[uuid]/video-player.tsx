'use client';

import { useEffect, useRef, useState, useCallback, useMemo, MouseEvent as ReactMouseEvent, TouchEvent as ReactTouchEvent } from 'react';
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
    Link2,
    Timer,
    Users,
} from 'lucide-react';
import { toast } from 'sonner';
import { formatElapsed } from '@/lib/utils';

interface ViewerPoint {
    offsetMillis: number;
    viewerCount: number;
}

interface VideoPlayerHeaderInfo {
    title: string;
    channelName: string;
    channelProfileUrl?: string;
    viewerCount?: number;
    streamStartedAt?: string;
}

interface VideoPlayerProps {
    playlistUrl: string;
    onTimeUpdate?: (currentTimeMs: number) => void;
    initialPosition?: number | null; // 초 단위
    seekRequest?: { seconds: number; nonce: number } | null;
    isLive?: boolean;
    isWide?: boolean;
    onWideToggle?: (isWide: boolean) => void;
    viewerHistory?: ViewerPoint[];
    headerInfo?: VideoPlayerHeaderInfo;
}

type FullscreenDocument = Document & {
    webkitFullscreenElement?: Element | null;
    webkitExitFullscreen?: () => Promise<void> | void;
};

type FullscreenElement = HTMLElement & {
    webkitRequestFullscreen?: () => Promise<void> | void;
};

type WebkitVideoElement = HTMLVideoElement & {
    autoPictureInPicture?: boolean;
    webkitDisplayingFullscreen?: boolean;
    webkitEnterFullscreen?: () => void;
    webkitExitFullscreen?: () => void;
};

type SeekDirection = 'backward' | 'forward';

interface MobileTap {
    time: number;
    x: number;
    y: number;
    direction: SeekDirection;
    wasControlsVisible: boolean;
}

interface TimelineTouchDrag {
    startX: number;
    startTime: number;
}

interface ContextMenuPosition {
    x: number;
    y: number;
}

const HIDE_DELAY_MS = 3000;
const LIVE_EDGE_THRESHOLD_SEC = 15; // 이 거리 이내면 "최신 지점"으로 보고 LIVE 표시
const SEEK_STEP_SEC = 5;
const SEEK_STEP_LONG_SEC = 10;
const MOBILE_DOUBLE_TAP_DELAY_MS = 300;
const MOBILE_SINGLE_TAP_DELAY_MS = 120;
const MOBILE_DOUBLE_TAP_MAX_DISTANCE_PX = 72;
const VOLUME_STEP = 0.05;
const PLAY_PAUSE_INDICATOR_MS = 1000;
const SEEK_INDICATOR_MS = 1000;
const VOLUME_STORAGE_KEY = 'video-player:volume';
const MUTED_STORAGE_KEY = 'video-player:muted';
const CONTEXT_MENU_WIDTH_PX = 240;
const CONTEXT_MENU_HEIGHT_PX = 96;
const CONTEXT_MENU_VIEWPORT_MARGIN_PX = 8;

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

// 녹화 시작 시각 + 재생 오프셋(초) → 그 당시 실제 시각 (예: "오후 2:32")
function formatWallClock(startedAt: string | undefined, offsetSeconds: number): string | null {
    if (!startedAt || !isFinite(offsetSeconds) || offsetSeconds < 0) return null;
    const startMs = new Date(startedAt).getTime();
    if (!Number.isFinite(startMs)) return null;
    const wall = new Date(startMs + offsetSeconds * 1000);
    let hours = wall.getHours();
    const minutes = wall.getMinutes();
    const period = hours < 12 ? '오전' : '오후';
    hours = hours % 12;
    if (hours === 0) hours = 12;
    return `${period} ${hours}:${minutes.toString().padStart(2, '0')}`;
}

function getFiniteDuration(video: HTMLMediaElement): number {
    return Number.isFinite(video.duration) && video.duration > 0 ? video.duration : 0;
}

function isTouchOnlyPointer(): boolean {
    return typeof window !== 'undefined' && window.matchMedia('(hover: none), (pointer: coarse)').matches;
}

function hasBrowserShortcutModifier(e: KeyboardEvent): boolean {
    return e.metaKey || e.ctrlKey || e.altKey;
}

function getShareUrl(seconds?: number): string {
    const url = new URL(window.location.href);
    if (seconds === undefined) {
        url.searchParams.delete('t');
    } else {
        url.searchParams.set('t', String(Math.max(0, Math.floor(seconds))));
    }
    url.hash = '';
    return url.toString();
}

async function copyText(text: string): Promise<void> {
    if (!navigator.clipboard || !window.isSecureContext) {
        throw new Error('Clipboard API is unavailable in this context');
    }

    await navigator.clipboard.writeText(text);
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
    seekRequest,
    isLive = false,
    isWide: isWideProp,
    onWideToggle,
    viewerHistory,
    headerInfo,
}: VideoPlayerProps) {
    const videoRef = useRef<HTMLVideoElement>(null);
    const containerRef = useRef<HTMLDivElement>(null);
    const controlsRef = useRef<HTMLDivElement>(null);
    const timelineRef = useRef<HTMLDivElement>(null);
    const hlsRef = useRef<Hls | null>(null);
    const hasRestoredPositionRef = useRef(false);
    const hideTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
    const isHoveringControlsRef = useRef(false);
    const isDraggingTimelineRef = useRef(false);
    const isControlsVisibleRef = useRef(true);
    const previousVolumeRef = useRef(1);
    const timelineTouchDragRef = useRef<TimelineTouchDrag | null>(null);

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
    const lastMobileTapRef = useRef<MobileTap | null>(null);
    const mobileSingleTapTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
    const mobileTapResetTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

    const [playPauseIndicator, setPlayPauseIndicator] = useState<{ kind: 'play' | 'pause'; nonce: number } | null>(null);
    const playPauseIndicatorTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

    const [seekIndicator, setSeekIndicator] = useState<{ direction: SeekDirection; seconds: number } | null>(null);
    const seekIndicatorTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
    const [contextMenuPosition, setContextMenuPosition] = useState<ContextMenuPosition | null>(null);

    useEffect(() => {
        setIsPipSupported(!!document.pictureInPictureEnabled);
    }, []);

    useEffect(() => {
        isControlsVisibleRef.current = isControlsVisible;
    }, [isControlsVisible]);

    useEffect(() => {
        return () => {
            if (mobileSingleTapTimerRef.current) {
                clearTimeout(mobileSingleTapTimerRef.current);
            }
            if (mobileTapResetTimerRef.current) {
                clearTimeout(mobileTapResetTimerRef.current);
            }
        };
    }, []);

    // HLS 로드
    useEffect(() => {
        if (!videoRef.current) return;
        const video = videoRef.current as WebkitVideoElement;

        if ('autoPictureInPicture' in video) {
            video.autoPictureInPicture = true;
        }

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

        if (Hls.isSupported()) {
            const hls = new Hls({ enableWorker: true, lowLatencyMode: false });
            hlsRef.current = hls;
            hls.loadSource(playlistUrl);
            hls.attachMedia(video);
            hls.on(Hls.Events.ERROR, (_event, data) => {
                if (data.fatal) {
                    console.error('HLS fatal error:', data);
                }
            });
        } else if (video.canPlayType('application/vnd.apple.mpegurl')) {
            video.src = playlistUrl;
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
        if (!videoRef.current || !initialPosition || !Number.isFinite(initialPosition) || hasRestoredPositionRef.current) return;
        const video = videoRef.current;
        const handleCanPlay = () => {
            if (!hasRestoredPositionRef.current && Number.isFinite(initialPosition) && initialPosition > 0) {
                video.currentTime = initialPosition;
                hasRestoredPositionRef.current = true;
            }
        };
        video.addEventListener('canplay', handleCanPlay);
        return () => video.removeEventListener('canplay', handleCanPlay);
    }, [initialPosition]);

    // 풀스크린 변경 감지
    useEffect(() => {
        const fullscreenDocument = document as FullscreenDocument;
        const video = videoRef.current;
        const handler = () => {
            setIsFullscreen(!!document.fullscreenElement || !!fullscreenDocument.webkitFullscreenElement);
        };
        const handleVideoEnterFullscreen = () => {
            setIsFullscreen(true);
        };
        const handleVideoExitFullscreen = () => {
            setIsFullscreen(false);
        };

        document.addEventListener('fullscreenchange', handler);
        document.addEventListener('webkitfullscreenchange', handler);
        video?.addEventListener('webkitbeginfullscreen', handleVideoEnterFullscreen);
        video?.addEventListener('webkitendfullscreen', handleVideoExitFullscreen);

        return () => {
            document.removeEventListener('fullscreenchange', handler);
            document.removeEventListener('webkitfullscreenchange', handler);
            video?.removeEventListener('webkitbeginfullscreen', handleVideoEnterFullscreen);
            video?.removeEventListener('webkitendfullscreen', handleVideoExitFullscreen);
        };
    }, []);

    // 자동 숨김 처리
    const showControls = useCallback(() => {
        isControlsVisibleRef.current = true;
        setIsControlsVisible(true);
        if (hideTimerRef.current) clearTimeout(hideTimerRef.current);

        const video = videoRef.current;
        if (!video) return;

        // 일시정지/드래깅/컨트롤 hover 중에는 숨기지 않음
        if (video.paused || isDraggingTimelineRef.current || isHoveringControlsRef.current) {
            return;
        }

        hideTimerRef.current = setTimeout(() => {
            if (!isHoveringControlsRef.current && !isDraggingTimelineRef.current) {
                const v = videoRef.current;
                if (v && !v.paused) {
                    isControlsVisibleRef.current = false;
                    setIsControlsVisible(false);
                }
            }
        }, HIDE_DELAY_MS);
    }, []);

    const hideControlsImmediately = useCallback(() => {
        if (hideTimerRef.current) clearTimeout(hideTimerRef.current);
        const video = videoRef.current;
        if (video && !video.paused && !isHoveringControlsRef.current && !isDraggingTimelineRef.current) {
            isControlsVisibleRef.current = false;
            setIsControlsVisible(false);
        }
    }, []);

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
    const showSeekIndicator = useCallback((direction: SeekDirection, step: number) => {
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
        if (!Number.isFinite(time)) return;
        const duration = getFiniteDuration(video);
        const clamped = Math.max(0, duration > 0 ? Math.min(duration, time) : time);
        video.currentTime = clamped;
        setCurrentTime(clamped);
    }, []);

    useEffect(() => {
        if (!seekRequest) return;
        seekTo(seekRequest.seconds);
        showControls();
    }, [seekRequest, seekTo, showControls]);

    // 풀스크린 토글
    const toggleFullscreen = useCallback(() => {
        const container = containerRef.current as FullscreenElement | null;
        const video = videoRef.current as WebkitVideoElement | null;
        const fullscreenDocument = document as FullscreenDocument;

        if (!container || !video) return;

        if (document.fullscreenElement || fullscreenDocument.webkitFullscreenElement) {
            if (document.exitFullscreen) {
                document.exitFullscreen().catch(() => {});
                return;
            }
            fullscreenDocument.webkitExitFullscreen?.();
            return;
        }

        if (video.webkitDisplayingFullscreen) {
            video.webkitExitFullscreen?.();
            return;
        }

        if (container.requestFullscreen) {
            container.requestFullscreen().catch(() => {
                video.webkitEnterFullscreen?.();
            });
        } else if (container.webkitRequestFullscreen) {
            try {
                const result = container.webkitRequestFullscreen();
                if (result instanceof Promise) {
                    result.catch(() => video.webkitEnterFullscreen?.());
                }
            } catch {
                video.webkitEnterFullscreen?.();
            }
        } else {
            video.webkitEnterFullscreen?.();
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

    const exitWide = useCallback(() => {
        if (!isWide) return;
        if (isWideProp === undefined) {
            setIsWideInternal(false);
        }
        if (onWideToggle) onWideToggle(false);
    }, [isWide, isWideProp, onWideToggle]);

    // 비디오 이벤트
    useEffect(() => {
        const video = videoRef.current;
        if (!video) return;

        const handlePlay = () => {
            setIsPlaying(true);
            showControls();
            if (isUserActionRef.current) {
                if (!isTouchOnlyPointer()) {
                    showPlayPauseIndicator('play');
                }
                isUserActionRef.current = false;
            }
        };
        const handlePause = () => {
            setIsPlaying(false);
            if (isUserActionRef.current) {
                if (!isTouchOnlyPointer()) {
                    showPlayPauseIndicator('pause');
                }
                isUserActionRef.current = false;
            }
        };
        const handleTimeUpdate = () => {
            setCurrentTime(video.currentTime);
            if (onTimeUpdate) onTimeUpdate(video.currentTime * 1000);
        };
        const handleDurationChange = () => setDuration(getFiniteDuration(video));
        const handleVolumeChange = () => {
            setVolume(video.volume);
            setIsMuted(video.muted);
        };
        const handleLoadedMetadata = () => {
            setDuration(getFiniteDuration(video));
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
            if (hasBrowserShortcutModifier(e)) {
                return;
            }

            const target = e.target as HTMLElement;
            // input, textarea, contentEditable에서는 동작 안 함
            if (
                target.tagName === 'INPUT' ||
                target.tagName === 'TEXTAREA' ||
                target.isContentEditable
            ) {
                return;
            }

            switch (e.code) {
                case 'Escape':
                    if (isWide && !contextMenuPosition) {
                        e.preventDefault();
                        exitWide();
                        showControls();
                    }
                    break;
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
                    const duration = getFiniteDuration(video);
                    if (duration > 0) {
                        const digit = parseInt(e.code.slice(-1), 10);
                        seekTo(duration * (digit / 10));
                        showControls();
                    }
                    break;
                }
            }
        };

        window.addEventListener('keydown', handleKeyDown);
        return () => window.removeEventListener('keydown', handleKeyDown);
    }, [togglePlay, seekTo, changeVolume, toggleMute, toggleFullscreen, toggleWide, exitWide, isWide, contextMenuPosition, showControls, showSeekIndicator]);

    // 타임라인 좌표 -> 시간 변환
    const timelineXToTime = useCallback((clientX: number): number => {
        const timeline = timelineRef.current;
        if (!timeline || !duration) return 0;
        const rect = timeline.getBoundingClientRect();
        const ratio = Math.max(0, Math.min(1, (clientX - rect.left) / rect.width));
        return ratio * duration;
    }, [duration]);

    const updateTimelineHover = useCallback((clientX: number) => {
        const timeline = timelineRef.current;
        if (!timeline || !duration) return;
        const rect = timeline.getBoundingClientRect();
        const x = Math.max(0, Math.min(rect.width, clientX - rect.left));
        setHoverX(x);
        setHoverTime(timelineXToTime(clientX));
    }, [duration, timelineXToTime]);

    const updateTimelinePreviewTime = useCallback((time: number) => {
        const timeline = timelineRef.current;
        if (!timeline || !duration) return;
        const rect = timeline.getBoundingClientRect();
        const clamped = Math.max(0, Math.min(duration, time));
        setHoverTime(clamped);
        setHoverX((clamped / duration) * rect.width);
    }, [duration]);

    // 타임라인 마우스 다운 (드래그 시작)
    const handleTimelineMouseDown = useCallback((e: ReactMouseEvent<HTMLDivElement>) => {
        e.stopPropagation();
        if (!duration) return;
        isDraggingTimelineRef.current = true;
        setIsDraggingTimeline(true);
        updateTimelineHover(e.clientX);
        seekTo(timelineXToTime(e.clientX));
    }, [duration, seekTo, timelineXToTime, updateTimelineHover]);

    // 타임라인 터치 시작 (모바일 드래그)
    const handleTimelineTouchStart = useCallback((e: ReactTouchEvent<HTMLDivElement>) => {
        e.stopPropagation();
        if (!duration || e.touches.length === 0) return;
        e.preventDefault();
        const touch = e.touches[0];
        const video = videoRef.current;
        if (!video) return;
        isDraggingTimelineRef.current = true;
        timelineTouchDragRef.current = {
            startX: touch.clientX,
            startTime: video.currentTime,
        };
        setIsDraggingTimeline(true);
        updateTimelinePreviewTime(video.currentTime);
        isControlsVisibleRef.current = true;
        setIsControlsVisible(true);
    }, [duration, updateTimelinePreviewTime]);

    // 드래그 중 포인터 이동/종료
    useEffect(() => {
        if (!isDraggingTimeline) return;

        const handleMove = (e: MouseEvent) => {
            updateTimelineHover(e.clientX);
            seekTo(timelineXToTime(e.clientX));
        };
        const handleTouchMove = (e: TouchEvent) => {
            if (e.touches.length === 0) return;
            e.preventDefault();
            const touch = e.touches[0];
            const timeline = timelineRef.current;
            const touchDrag = timelineTouchDragRef.current;
            if (!timeline || !touchDrag || !duration) return;
            const rect = timeline.getBoundingClientRect();
            const movedSeconds = ((touch.clientX - touchDrag.startX) / rect.width) * duration;
            const nextTime = touchDrag.startTime + movedSeconds;
            updateTimelinePreviewTime(nextTime);
            seekTo(nextTime);
        };
        const handleUp = () => {
            isDraggingTimelineRef.current = false;
            timelineTouchDragRef.current = null;
            setIsDraggingTimeline(false);
            setHoverTime(null);
            showControls();
        };

        window.addEventListener('mousemove', handleMove);
        window.addEventListener('mouseup', handleUp);
        window.addEventListener('touchmove', handleTouchMove, { passive: false });
        window.addEventListener('touchend', handleUp);
        window.addEventListener('touchcancel', handleUp);
        return () => {
            window.removeEventListener('mousemove', handleMove);
            window.removeEventListener('mouseup', handleUp);
            window.removeEventListener('touchmove', handleTouchMove);
            window.removeEventListener('touchend', handleUp);
            window.removeEventListener('touchcancel', handleUp);
        };
    }, [duration, isDraggingTimeline, seekTo, showControls, timelineXToTime, updateTimelineHover, updateTimelinePreviewTime]);

    // 타임라인 hover
    const handleTimelineMouseMove = useCallback((e: ReactMouseEvent<HTMLDivElement>) => {
        updateTimelineHover(e.clientX);
    }, [updateTimelineHover]);

    const handleTimelineMouseLeave = useCallback(() => {
        setHoverTime(null);
    }, []);

    // 컨테이너 마우스 인터랙션
    const handleContainerMouseMove = useCallback(() => {
        if (isTouchOnlyPointer()) return;
        if (contextMenuPosition) return;
        showControls();
    }, [contextMenuPosition, showControls]);

    const handleContainerMouseLeave = useCallback(() => {
        if (isTouchOnlyPointer()) return;
        hideControlsImmediately();
    }, [hideControlsImmediately]);

    const scheduleMobileSingleTapControls = useCallback(() => {
        if (mobileSingleTapTimerRef.current) {
            clearTimeout(mobileSingleTapTimerRef.current);
            mobileSingleTapTimerRef.current = null;
        }

        mobileSingleTapTimerRef.current = setTimeout(() => {
            mobileSingleTapTimerRef.current = null;

            const video = videoRef.current;
            if (isControlsVisibleRef.current && video && !video.paused) {
                isControlsVisibleRef.current = false;
                setIsControlsVisible(false);
                return;
            }

            showControls();
        }, MOBILE_SINGLE_TAP_DELAY_MS);
    }, [showControls]);

    // 모바일 탭은 컨트롤 토글, 좌/우 더블탭은 시킹으로 처리
    const handleContainerTouchStart = useCallback((e: ReactTouchEvent<HTMLDivElement>) => {
        if (!isTouchOnlyPointer()) return;
        const controls = controlsRef.current;
        if (controls && controls.contains(e.target as Node)) {
            lastMobileTapRef.current = null;
            if (mobileSingleTapTimerRef.current) {
                clearTimeout(mobileSingleTapTimerRef.current);
                mobileSingleTapTimerRef.current = null;
            }
            if (mobileTapResetTimerRef.current) {
                clearTimeout(mobileTapResetTimerRef.current);
                mobileTapResetTimerRef.current = null;
            }
            return;
        }
        if (e.touches.length !== 1) {
            lastMobileTapRef.current = null;
            if (mobileSingleTapTimerRef.current) {
                clearTimeout(mobileSingleTapTimerRef.current);
                mobileSingleTapTimerRef.current = null;
            }
            if (mobileTapResetTimerRef.current) {
                clearTimeout(mobileTapResetTimerRef.current);
                mobileTapResetTimerRef.current = null;
            }
            return;
        }

        const touch = e.touches[0];
        const container = containerRef.current;
        const rect = container?.getBoundingClientRect();
        const direction: SeekDirection = rect && touch.clientX < rect.left + rect.width / 2 ? 'backward' : 'forward';
        const now = Date.now();
        const lastTap = lastMobileTapRef.current;
        const tapDistance = lastTap ? Math.hypot(touch.clientX - lastTap.x, touch.clientY - lastTap.y) : Infinity;
        const isDoubleTap =
            !!lastTap &&
            lastTap.direction === direction &&
            now - lastTap.time <= MOBILE_DOUBLE_TAP_DELAY_MS &&
            tapDistance <= MOBILE_DOUBLE_TAP_MAX_DISTANCE_PX;
        const isSingleTapPending = mobileSingleTapTimerRef.current !== null;

        if (hideTimerRef.current) clearTimeout(hideTimerRef.current);

        if (isDoubleTap) {
            e.preventDefault();
            lastMobileTapRef.current = null;
            if (mobileSingleTapTimerRef.current) {
                clearTimeout(mobileSingleTapTimerRef.current);
                mobileSingleTapTimerRef.current = null;
            }
            if (mobileTapResetTimerRef.current) {
                clearTimeout(mobileTapResetTimerRef.current);
                mobileTapResetTimerRef.current = null;
            }
            const video = videoRef.current;
            if (video) {
                const step = direction === 'backward' ? -SEEK_STEP_LONG_SEC : SEEK_STEP_LONG_SEC;
                seekTo(video.currentTime + step);
                showSeekIndicator(direction, SEEK_STEP_LONG_SEC);
            }

            const currentVideo = videoRef.current;
            const isControlsCurrentlyVisible = isControlsVisibleRef.current;
            if (isControlsCurrentlyVisible && currentVideo && !currentVideo.paused) {
                scheduleMobileSingleTapControls();
            } else if (!isControlsCurrentlyVisible && !isSingleTapPending && !lastTap.wasControlsVisible) {
                showControls();
            }
            return;
        }

        if (mobileSingleTapTimerRef.current) {
            clearTimeout(mobileSingleTapTimerRef.current);
            mobileSingleTapTimerRef.current = null;
        }
        if (mobileTapResetTimerRef.current) {
            clearTimeout(mobileTapResetTimerRef.current);
            mobileTapResetTimerRef.current = null;
        }
        lastMobileTapRef.current = {
            time: now,
            x: touch.clientX,
            y: touch.clientY,
            direction,
            wasControlsVisible: isControlsVisibleRef.current,
        };

        scheduleMobileSingleTapControls();

        mobileTapResetTimerRef.current = setTimeout(() => {
            lastMobileTapRef.current = null;
            mobileTapResetTimerRef.current = null;
        }, MOBILE_DOUBLE_TAP_DELAY_MS);
    }, [scheduleMobileSingleTapControls, seekTo, showControls, showSeekIndicator]);

    const closeContextMenu = useCallback(() => {
        setContextMenuPosition(null);
    }, []);

    // 컨테이너 클릭 (재생/일시정지) — 더블클릭과 구분하기 위해 딜레이 후 실행
    const handleContainerClick = useCallback((e: ReactMouseEvent<HTMLDivElement>) => {
        if (isTouchOnlyPointer()) return;
        if (contextMenuPosition) {
            closeContextMenu();
            return;
        }
        const controls = controlsRef.current;
        if (controls && controls.contains(e.target as Node)) return;
        if (clickTimerRef.current) clearTimeout(clickTimerRef.current);
        clickTimerRef.current = setTimeout(() => {
            togglePlay();
        }, 200);
    }, [closeContextMenu, contextMenuPosition, togglePlay]);

    // 더블클릭 (풀스크린) — 싱글클릭 타이머를 취소하고 풀스크린 실행
    const handleContainerDoubleClick = useCallback((e: ReactMouseEvent<HTMLDivElement>) => {
        if (isTouchOnlyPointer()) return;
        if (contextMenuPosition) {
            closeContextMenu();
            return;
        }
        const controls = controlsRef.current;
        if (controls && controls.contains(e.target as Node)) return;
        if (clickTimerRef.current) {
            clearTimeout(clickTimerRef.current);
            clickTimerRef.current = null;
        }
        toggleFullscreen();
    }, [closeContextMenu, contextMenuPosition, toggleFullscreen]);

    const handleContainerContextMenu = useCallback((e: ReactMouseEvent<HTMLDivElement>) => {
        if (isTouchOnlyPointer()) return;
        e.preventDefault();
        e.stopPropagation();

        if (clickTimerRef.current) {
            clearTimeout(clickTimerRef.current);
            clickTimerRef.current = null;
        }

        const x = Math.min(e.clientX, window.innerWidth - CONTEXT_MENU_WIDTH_PX - CONTEXT_MENU_VIEWPORT_MARGIN_PX);
        const y = Math.min(e.clientY, window.innerHeight - CONTEXT_MENU_HEIGHT_PX - CONTEXT_MENU_VIEWPORT_MARGIN_PX);
        setContextMenuPosition({
            x: Math.max(CONTEXT_MENU_VIEWPORT_MARGIN_PX, x),
            y: Math.max(CONTEXT_MENU_VIEWPORT_MARGIN_PX, y),
        });
        showControls();
    }, [showControls]);

    useEffect(() => {
        if (!contextMenuPosition) return;

        const handleKeyDown = (e: KeyboardEvent) => {
            if (e.key === 'Escape') {
                closeContextMenu();
            }
        };

        window.addEventListener('click', closeContextMenu);
        window.addEventListener('resize', closeContextMenu);
        window.addEventListener('scroll', closeContextMenu, true);
        window.addEventListener('keydown', handleKeyDown);
        return () => {
            window.removeEventListener('click', closeContextMenu);
            window.removeEventListener('resize', closeContextMenu);
            window.removeEventListener('scroll', closeContextMenu, true);
            window.removeEventListener('keydown', handleKeyDown);
        };
    }, [closeContextMenu, contextMenuPosition]);

    const handleCopyVideoUrl = useCallback(async () => {
        try {
            await copyText(getShareUrl());
            toast.success('동영상 URL이 복사되었습니다.');
        } catch (error) {
            console.error(error);
            toast.error('URL 복사에 실패했습니다.');
        } finally {
            closeContextMenu();
        }
    }, [closeContextMenu]);

    const handleCopyCurrentTimeVideoUrl = useCallback(async () => {
        const video = videoRef.current;
        const seconds = video ? video.currentTime : currentTime;

        try {
            await copyText(getShareUrl(seconds));
            toast.success(`현재 시간 URL이 복사되었습니다. (${formatTime(seconds)})`);
        } catch (error) {
            console.error(error);
            toast.error('URL 복사에 실패했습니다.');
        } finally {
            closeContextMenu();
        }
    }, [closeContextMenu, currentTime]);

    // 컨트롤 hover 상태 추적
    const handleControlsEnter = useCallback(() => {
        isHoveringControlsRef.current = true;
        if (hideTimerRef.current) clearTimeout(hideTimerRef.current);
        isControlsVisibleRef.current = true;
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

    // 녹화 시작 시각 (그 당시 실제 시각 계산용)
    const recordStartedAt = headerInfo?.streamStartedAt;

    // 라이브 중 최신 지점(live edge) 근처인지 여부.
    // 최신이면 LIVE, 뒤로 돌려보는 중이면 시간을 표시한다.
    const isAtLiveEdge = isLive && (duration <= 0 || duration - currentTime <= LIVE_EDGE_THRESHOLD_SEC);

    // 현재 재생 시점의 실제 시각.
    // VOD는 항상, 라이브는 뒤로 돌려본(live edge가 아닌) 경우에만 표시.
    const currentWallClock = useMemo(
        () => (isAtLiveEdge ? null : formatWallClock(recordStartedAt, currentTime)),
        [isAtLiveEdge, recordStartedAt, currentTime],
    );

    // hover 시점의 실제 시각
    const hoverWallClock = useMemo(
        () => (hoverTime === null ? null : formatWallClock(recordStartedAt, hoverTime)),
        [hoverTime, recordStartedAt],
    );

    // 타임라인 hover 툴팁 X 좌표 clamp
    const tooltipX = useMemo(() => {
        const timeline = timelineRef.current;
        if (!timeline) return hoverX;
        const width = timeline.getBoundingClientRect().width;
        // 툴팁 절반 너비 약 32px 가정, 가장자리에서 잘림 방지
        const half = 32;
        return Math.max(half, Math.min(width - half, hoverX));
    }, [hoverX]);

    // 헤더 표시 여부: 넓은 화면 또는 전체화면일 때만
    const showHeader = !!headerInfo && (isWide || isFullscreen);

    // 스트리밍 경과 시간 (헤더가 보이는 라이브 상태에서만 1초마다 갱신)
    const [streamElapsedSec, setStreamElapsedSec] = useState<number | null>(null);
    useEffect(() => {
        const startedAt = headerInfo?.streamStartedAt;
        if (!showHeader || !isLive || !startedAt) {
            setStreamElapsedSec(null);
            return;
        }
        const startMs = new Date(startedAt).getTime();
        if (!Number.isFinite(startMs)) {
            setStreamElapsedSec(null);
            return;
        }
        const update = () => setStreamElapsedSec(Math.max(0, (Date.now() - startMs) / 1000));
        update();
        const interval = setInterval(update, 1000);
        return () => clearInterval(interval);
    }, [showHeader, isLive, headerInfo?.streamStartedAt]);

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
            } touch-manipulation`}
            onMouseMove={handleContainerMouseMove}
            onMouseLeave={handleContainerMouseLeave}
            onTouchStart={handleContainerTouchStart}
            onClick={handleContainerClick}
            onDoubleClick={handleContainerDoubleClick}
            onContextMenu={handleContainerContextMenu}
            style={{ cursor: isControlsVisible ? 'default' : 'none' }}
        >
            <video
                ref={videoRef}
                className="w-full h-full object-contain"
                playsInline
                webkit-playsinline=""
            />

            {/* 상단 정보 헤더 (넓은 화면/전체화면 시) */}
            {showHeader && headerInfo && (
                <div
                    className={`absolute inset-x-0 top-0 z-10 transition-opacity duration-200 ${
                        isControlsVisible ? 'opacity-100' : 'opacity-0 pointer-events-none'
                    }`}
                >
                    {/* 그라디언트 배경 */}
                    <div className="absolute inset-x-0 top-0 h-44 bg-gradient-to-b from-black/75 via-black/40 to-transparent pointer-events-none" />

                    <div className="relative flex items-center gap-4 px-4 pt-4">
                        {/* 채널 아이콘 */}
                        {headerInfo.channelProfileUrl ? (
                            // eslint-disable-next-line @next/next/no-img-element
                            <img
                                src={headerInfo.channelProfileUrl}
                                alt=""
                                className="h-16 w-16 flex-shrink-0 rounded-full object-cover ring-1 ring-white/25"
                            />
                        ) : (
                            <div className="flex h-16 w-16 flex-shrink-0 items-center justify-center rounded-full bg-white/20 text-xl font-semibold text-white ring-1 ring-white/25">
                                {headerInfo.channelName.charAt(0)}
                            </div>
                        )}

                        {/* 제목 / 채널 이름 / 라이브 메타 */}
                        <div className="min-w-0 flex-1">
                            <p className="truncate text-xl font-semibold leading-snug text-white drop-shadow-[0_1px_3px_rgba(0,0,0,0.8)]">
                                {headerInfo.title}
                            </p>
                            <p className="mt-1 truncate text-sm font-medium text-white/90 drop-shadow-[0_1px_2px_rgba(0,0,0,0.8)]">
                                {headerInfo.channelName}
                            </p>
                            {isLive && (
                                <div className="mt-1 flex items-center gap-3 text-sm font-medium text-white drop-shadow-[0_1px_2px_rgba(0,0,0,0.8)]">
                                    <span className="flex flex-shrink-0 items-center gap-1.5">
                                        <Users size={16} />
                                        <span className="tabular-nums">
                                            {(headerInfo.viewerCount ?? 0).toLocaleString('ko-KR')}명 시청 중
                                        </span>
                                    </span>
                                    {streamElapsedSec !== null && (
                                        <span className="flex flex-shrink-0 items-center gap-1.5">
                                            <span className="inline-block h-2 w-2 rounded-full bg-red-500" />
                                            <span className="tabular-nums">
                                                {formatElapsed(streamElapsedSec)} 스트리밍 중
                                            </span>
                                        </span>
                                    )}
                                </div>
                            )}
                        </div>
                    </div>
                </div>
            )}

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

            {/* 우클릭 메뉴 */}
            {contextMenuPosition !== null && (
                <div
                    className="fixed z-50 w-60 overflow-hidden rounded-md bg-neutral-950/70 py-2 text-sm text-white shadow-xl backdrop-blur-xl"
                    style={{
                        left: contextMenuPosition.x,
                        top: contextMenuPosition.y,
                    }}
                    onClick={(e) => e.stopPropagation()}
                    onContextMenu={(e) => e.preventDefault()}
                >
                    <button
                        type="button"
                        className="flex w-full cursor-pointer items-center gap-3 px-3 py-2 text-left transition-colors hover:bg-white/10 focus:bg-white/10 focus:outline-none"
                        onClick={handleCopyVideoUrl}
                    >
                        <Link2 className="h-4 w-4 text-white/70" />
                        <span>동영상 URL 복사</span>
                    </button>
                    <button
                        type="button"
                        className="flex w-full cursor-pointer items-center gap-3 px-3 py-2 text-left transition-colors hover:bg-white/10 focus:bg-white/10 focus:outline-none"
                        onClick={handleCopyCurrentTimeVideoUrl}
                    >
                        <Timer className="h-4 w-4 text-white/70" />
                        <span>현재 시간 동영상 URL 복사</span>
                    </button>
                </div>
            )}

            {/* 모바일 중앙 재생/일시정지 버튼 */}
            <div
                className={`absolute inset-0 z-10 hidden items-center justify-center pointer-events-none transition-opacity duration-200 max-md:flex ${
                    isControlsVisible ? 'opacity-100' : 'opacity-0'
                }`}
            >
                <button
                    type="button"
                    className={`flex h-16 w-16 items-center justify-center rounded-full bg-black/50 text-white shadow-lg backdrop-blur-sm active:scale-95 transition-transform ${
                        isControlsVisible ? 'pointer-events-auto' : 'pointer-events-none'
                    }`}
                    aria-label={isPlaying ? '일시정지' : '재생'}
                    onTouchStart={(e) => e.stopPropagation()}
                    onClick={(e) => {
                        e.stopPropagation();
                        togglePlay();
                        showControls();
                    }}
                >
                    {isPlaying ? <Pause size={34} fill="currentColor" /> : <Play size={34} fill="currentColor" />}
                </button>
            </div>

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
                    className="relative px-4 pt-2 touch-none"
                    onMouseEnter={handleControlsEnter}
                    onMouseLeave={handleControlsLeave}
                    onTouchStart={handleTimelineTouchStart}
                >
                    <div
                        ref={timelineRef}
                        className="relative h-1.5 bg-white/20 rounded-full cursor-pointer group/timeline hover:bg-white/40 transition-colors touch-none"
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
                            className={`absolute top-1/2 -translate-y-1/2 -translate-x-1/2 w-3.5 h-3.5 bg-white rounded-full transition-opacity pointer-events-none ${
                                isControlsVisible || isDraggingTimeline ? 'opacity-100' : 'opacity-0 group-hover/timeline:opacity-100'
                            }`}
                            style={{ left: `${progressPercent}%` }}
                        />

                        {/* hover 시간/시청자 툴팁 */}
                        {hoverTime !== null && (
                            <div
                                className="absolute bottom-full mb-6 px-4 py-2 bg-black/50 text-white rounded-full pointer-events-none whitespace-nowrap text-center leading-tight"
                                style={{
                                    left: `${tooltipX}px`,
                                    transform: 'translateX(-50%)',
                                }}
                            >
                                <div className="text-sm font-bold tabular-nums">{formatTime(hoverTime)}</div>
                                {hoverWallClock !== null && (
                                    <div className="text-xs text-white/60 tabular-nums">{hoverWallClock}</div>
                                )}
                                {hoverViewerCount !== null && (
                                    <div className="text-xs text-white/70">
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
                            isAtLiveEdge ? (
                                <span className="flex items-center gap-1.5">
                                    <span className="inline-block w-2 h-2 bg-red-500 rounded-full animate-pulse" />
                                    <span className="font-semibold">LIVE</span>
                                </span>
                            ) : (
                                <button
                                    type="button"
                                    onClick={() => seekTo(Math.max(0, duration - 1))}
                                    className="flex items-center gap-2 hover:text-white/90 transition-colors"
                                    title="실시간으로 이동"
                                >
                                    <span className="flex items-center gap-1.5 text-white/60">
                                        <span className="inline-block w-2 h-2 bg-white/40 rounded-full" />
                                        <span className="font-semibold">LIVE</span>
                                    </span>
                                    {currentWallClock !== null && (
                                        <span className="text-white/50">{currentWallClock}</span>
                                    )}
                                </button>
                            )
                        ) : (
                            <span>
                                {formatTime(currentTime)} / {formatTime(duration)}
                                {currentWallClock !== null && (
                                    <span className="ml-2 text-white/50">{currentWallClock}</span>
                                )}
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
