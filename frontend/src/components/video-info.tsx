'use client';

import Link from 'next/link';
import {useRouter} from 'next/navigation';
import {useQueryClient} from '@tanstack/react-query';
import {Avatar, AvatarFallback, AvatarImage} from '@/components/ui/avatar';
import {PlatformBadge} from '@/components/common/platform-badge';
import {VideoFormDialog} from '@/components/admin/video-form-dialog';
import {Button} from '@/components/ui/button';
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import {Bookmark, BookmarkX, ChevronDown, Edit, History, Loader2, MoreVertical, Trash2, Users} from 'lucide-react';
import {useEffect, useMemo, useState} from 'react';
import {toast} from 'sonner';
import {formatElapsed} from '@/lib/utils';
import {
    useDeleteAdminVideo,
    useSearchAdminVideos,
    useSetArchivedAdminVideo,
    useUpdateAdminVideo,
} from '@/lib/api/endpoints/video-admin/video-admin';
import {useGetChannelPlatforms} from '@/lib/api/endpoints/channel/channel';
import type {VideoAdminUpdateRequestContentPrivacy, VideoChapterGetResponse, VideoGetResponse} from '@/lib/api/models';
import type {ReactNode} from 'react';
import {findCurrentChapter, toDisplayChapters} from '@/lib/chapters';

interface VideoInfoProps {
    video: VideoGetResponse;
    isAdmin?: boolean;
    onTimestampClick?: (seconds: number) => void;
    isLive?: boolean;
    viewerCount?: number;
    chapters?: VideoChapterGetResponse[];
    currentTimeMs?: number;
    /** 채널 줄의 타임라인 버튼을 누르면 호출한다. */
    onTimelineClick?: () => void;
}

function parseTimestampToSeconds(match: RegExpExecArray): number {
    const first = Number(match[2]);
    const second = Number(match[3]);
    const third = match[4] == null ? null : Number(match[4]);

    if (third == null) {
        return first * 60 + second;
    }

    return first * 3600 + second * 60 + third;
}

function renderDescription(description: string, onTimestampClick?: (seconds: number) => void): ReactNode {
    if (!onTimestampClick) {
        return description;
    }

    const timestampPattern = /(^|[^\d])(\d{1,3}):([0-5]\d)(?::([0-5]\d))?(?!\d)/g;
    const nodes: ReactNode[] = [];
    let lastIndex = 0;
    let match: RegExpExecArray | null;

    while ((match = timestampPattern.exec(description)) !== null) {
        const prefix = match[1];
        const timestampStart = match.index + prefix.length;
        const timestampText = description.slice(timestampStart, timestampPattern.lastIndex);
        const timestampSeconds = parseTimestampToSeconds(match);

        if (timestampStart > lastIndex) {
            nodes.push(description.slice(lastIndex, timestampStart));
        }

        nodes.push(
            <button
                key={`${timestampStart}-${timestampText}`}
                type="button"
                className="inline cursor-pointer rounded-sm font-medium text-link hover:opacity-80 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
                onClick={() => onTimestampClick(timestampSeconds)}
            >
                {timestampText}
            </button>
        );
        lastIndex = timestampPattern.lastIndex;
    }

    if (lastIndex < description.length) {
        nodes.push(description.slice(lastIndex));
    }

    return nodes.length > 0 ? nodes : description;
}

export function VideoInfo({ video, isAdmin = false, onTimestampClick, isLive = false, viewerCount, chapters, currentTimeMs, onTimelineClick }: VideoInfoProps) {
    const router = useRouter();
    const queryClient = useQueryClient();
    const [isExpanded, setIsExpanded] = useState(false);
    const [isDialogOpen, setIsDialogOpen] = useState(false);
    const recordedAt = video.record?.startedAt ?? video.createdAt;

    // 카테고리 변경 이력 기반 챕터 (duration 기준으로 가공, 카테고리가 없으면 빈 배열)
    const displayChapters = useMemo(
        () => toDisplayChapters(chapters, video.duration),
        [chapters, video.duration],
    );
    const currentChapter = useMemo(
        () => (currentTimeMs == null ? null : findCurrentChapter(displayChapters, currentTimeMs / 1000)),
        [displayChapters, currentTimeMs],
    );

    // 스트리밍 경과 시간 (라이브 중에만 1초마다 갱신)
    const streamStartedAt = video.record?.startedAt;
    const [streamElapsedSec, setStreamElapsedSec] = useState<number | null>(null);
    useEffect(() => {
        if (!isLive || !streamStartedAt) {
            setStreamElapsedSec(null);
            return;
        }
        const startMs = new Date(streamStartedAt).getTime();
        if (!Number.isFinite(startMs)) {
            setStreamElapsedSec(null);
            return;
        }
        const update = () => setStreamElapsedSec(Math.max(0, (Date.now() - startMs) / 1000));
        update();
        const interval = setInterval(update, 1000);
        return () => clearInterval(interval);
    }, [isLive, streamStartedAt]);
    const platformType = video.record?.platformType;
    const adminVideoParams = useMemo(() => ({
        request: {
            uuid: video.uuid,
        },
        pageable: {
            page: 0,
            size: 1,
        },
    }), [video.uuid]);
    const {data: adminVideosData, isLoading: isAdminVideoLoading} = useSearchAdminVideos(adminVideoParams, {
        query: {
            enabled: isAdmin,
        },
    });
    const {data: platforms} = useGetChannelPlatforms(video.channel.uuid, {
        query: {
            enabled: !!platformType,
        },
    });
    const platformLink = useMemo(
        () => platforms?.find((platform) => platform.platformType === platformType)?.streamUrl,
        [platforms, platformType]
    );
    const adminVideo = adminVideosData?.content?.[0] ?? null;
    const updateMutation = useUpdateAdminVideo();
    const deleteMutation = useDeleteAdminVideo();
    const archiveMutation = useSetArchivedAdminVideo();

    const formatDateTime = (dateString: string) => {
        return new Date(dateString).toLocaleString('ko-KR', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
        });
    };

    const formatFileSize = (bytes: number) => {
        if (bytes === 0) return '0 B';
        const k = 1024;
        const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return `${parseFloat((bytes / Math.pow(k, i)).toFixed(2))} ${sizes[i]}`;
    };

    // 최고 시청자수 시점으로 점프 (시점 데이터와 핸들러가 모두 있을 때만 클릭 가능)
    const peakViewerSeconds =
        video.peakViewerOffsetMillis != null ? Math.floor(video.peakViewerOffsetMillis / 1000) : null;
    const onPeakViewerClick =
        peakViewerSeconds != null && onTimestampClick ? () => onTimestampClick(peakViewerSeconds) : undefined;

    const handleDialogSubmit = async (data: { title: string; description: string; contentPrivacy: VideoAdminUpdateRequestContentPrivacy; chatSyncOffsetMillis: number }) => {
        if (!adminVideo) return;

        try {
            await updateMutation.mutateAsync({
                id: adminVideo.id,
                data,
            });
            toast.success('동영상이 수정되었습니다.');
            setIsDialogOpen(false);
            await queryClient.invalidateQueries({queryKey: ['/admin/videos']});
            router.refresh();
        } catch (error) {
            console.error(error);
            toast.error('동영상 수정에 실패했습니다.');
        }
    };

    const handleToggleArchive = async () => {
        if (!adminVideo) return;
        const next = !adminVideo.isArchived;
        try {
            await archiveMutation.mutateAsync({id: adminVideo.id, data: {isArchived: next}});
            toast.success(next ? '동영상이 소장되었습니다.' : '소장이 해제되었습니다.');
            await queryClient.invalidateQueries({queryKey: ['/admin/videos']});
            router.refresh();
        } catch (error) {
            console.error(error);
            toast.error('처리에 실패했습니다.');
        }
    };

    const handleDelete = async () => {
        if (!adminVideo) return;
        if (!confirm(`"${adminVideo.title}" 동영상을 삭제하시겠습니까?`)) {
            return;
        }

        try {
            await deleteMutation.mutateAsync({id: adminVideo.id});
            toast.success('동영상이 삭제되었습니다.');
            await queryClient.invalidateQueries({queryKey: ['/admin/videos']});
            router.push('/');
            router.refresh();
        } catch (error) {
            console.error(error);
            toast.error('동영상 삭제에 실패했습니다.');
        }
    };

    const adminMenu = isAdmin ? (
        <DropdownMenu>
            <DropdownMenuTrigger asChild>
                <Button
                    type="button"
                    variant="secondary"
                    size="icon"
                    className="h-9 w-9 flex-shrink-0 rounded-full"
                    aria-label="동영상 관리 메뉴"
                >
                    {isAdminVideoLoading ? (
                        <Loader2 className="h-4 w-4 animate-spin" />
                    ) : (
                        <MoreVertical className="h-5 w-5" />
                    )}
                </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
                <DropdownMenuItem
                    disabled={!adminVideo || archiveMutation.isPending}
                    onClick={handleToggleArchive}
                >
                    {adminVideo?.isArchived ? (
                        <>
                            <BookmarkX className="h-4 w-4" />
                            소장 해제
                        </>
                    ) : (
                        <>
                            <Bookmark className="h-4 w-4" />
                            소장
                        </>
                    )}
                </DropdownMenuItem>
                <DropdownMenuItem
                    disabled={!adminVideo || updateMutation.isPending}
                    onClick={() => setIsDialogOpen(true)}
                >
                    <Edit className="h-4 w-4" />
                    수정
                </DropdownMenuItem>
                <DropdownMenuItem
                    variant="destructive"
                    disabled={!adminVideo || deleteMutation.isPending}
                    onClick={handleDelete}
                >
                    <Trash2 className="h-4 w-4" />
                    삭제
                </DropdownMenuItem>
            </DropdownMenuContent>
        </DropdownMenu>
    ) : null;

    return (
        <>
            <div className="px-4 py-3">
                {/* 제목 */}
                <button
                    type="button"
                    className={`flex w-full items-start justify-between gap-3 text-left lg:pointer-events-none ${isLive ? 'mb-1.5' : 'mb-2'}`}
                    onClick={() => setIsExpanded((prev) => !prev)}
                    aria-expanded={isExpanded}
                >
                    <h1 className="min-w-0 flex-1 text-xl font-bold flex items-center gap-1.5">
                        <span className="min-w-0 truncate">{video.title}</span>
                        {video.isArchived && (
                            <Bookmark size={17} className="flex-shrink-0 text-muted-foreground opacity-50" fill="currentColor" />
                        )}
                    </h1>
                    <ChevronDown
                        className={`mt-1 h-5 w-5 flex-shrink-0 text-muted-foreground transition-transform lg:hidden ${isExpanded ? 'rotate-180' : ''}`}
                        aria-hidden="true"
                    />
                </button>

                {/* 라이브 정보 (녹화 중일 때만) */}
                {isLive && (
                    <div className="mb-4 flex items-center gap-3 text-sm font-medium text-foreground">
                        <span className="flex flex-shrink-0 items-center gap-1.5">
                            <Users size={16} />
                            <span className="tabular-nums">{(viewerCount ?? 0).toLocaleString('ko-KR')}명 시청 중</span>
                        </span>
                        {streamElapsedSec !== null && (
                            <span className="flex flex-shrink-0 items-center gap-1.5">
                                <span className="inline-block h-2 w-2 rounded-full bg-red-500" />
                                <span className="tabular-nums">{formatElapsed(streamElapsedSec)} 스트리밍 중</span>
                            </span>
                        )}
                    </div>
                )}

                {/* 채널 정보 */}
                <div className="flex items-center justify-between gap-3">
                    <Link
                        href={`/channels/${video.channel.uuid}`}
                        className="inline-flex min-w-0 max-w-full items-center gap-3"
                    >
                        <Avatar className="w-10 h-10">
                            <AvatarImage src={video.channel.profileUrl} />
                            <AvatarFallback>{video.channel.name[0]}</AvatarFallback>
                        </Avatar>
                        <div className="min-w-0">
                            <p className="truncate font-medium">{video.channel.name}</p>
                        </div>
                    </Link>
                    {/* 채널 줄 최우측 기능 버튼 (타임라인 + 관리 메뉴) */}
                    <div className="flex flex-shrink-0 items-center gap-1.5">
                        {onTimelineClick && (
                            <Button
                                type="button"
                                variant="secondary"
                                onClick={onTimelineClick}
                                className="gap-1.5 rounded-full"
                            >
                                <History className="h-4 w-4" />
                                타임라인
                            </Button>
                        )}
                        {adminMenu}
                    </div>
                </div>

                <div className={`${isExpanded ? 'block' : 'hidden'} mt-3 rounded-lg bg-muted p-3.5 lg:block`}>
                    {/* 핵심 정보 줄 (유튜브 설명란의 조회수·날짜 줄 포지션).
                        의미 단위로 묶는다 — 녹화물 속성(날짜·용량) / 방송 속성(최고 시청자·플랫폼).
                        모바일에서는 이 두 묶음이 의도된 두 줄이 되고, PC에서는 한 줄로 이어진다.
                        구분점 없이 간격(12px)과 항목별 색·무게 차이로 구분한다. */}
                    <div className="flex flex-col gap-1 text-sm font-medium sm:flex-row sm:flex-wrap sm:items-center sm:gap-x-3">
                        <div className="flex items-center gap-x-3">
                            <span>{formatDateTime(recordedAt)}</span>
                            <span className="text-muted-foreground">{formatFileSize(video.fileSize)}</span>
                        </div>
                        {(video.peakViewerCount != null || platformType) && (
                            <div className="flex items-center gap-x-3">
                                {video.peakViewerCount != null &&
                                    (onPeakViewerClick ? (
                                        <button
                                            type="button"
                                            onClick={onPeakViewerClick}
                                            className="cursor-pointer rounded-sm text-link hover:opacity-80 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
                                            title="최고 시청자 수 시점으로 이동"
                                        >
                                            최고 시청자 {video.peakViewerCount.toLocaleString('ko-KR')}명
                                        </button>
                                    ) : (
                                        <span>최고 시청자 {video.peakViewerCount.toLocaleString('ko-KR')}명</span>
                                    ))}
                                {platformType &&
                                    (platformLink ? (
                                        <a href={platformLink} target="_blank" rel="noopener noreferrer" className="inline-flex">
                                            <PlatformBadge platform={platformType} />
                                        </a>
                                    ) : (
                                        <PlatformBadge platform={platformType} />
                                    ))}
                            </div>
                        )}
                    </div>
                    {video.description && (
                        <p className="mt-3 whitespace-pre-wrap text-sm leading-6">
                            {renderDescription(video.description, onTimestampClick)}
                        </p>
                    )}
                    {displayChapters.length > 0 && (
                        <div className="mt-3 border-t border-border/60 pt-3">
                            <p className="mb-2 text-xs text-muted-foreground">챕터</p>
                            <ul className="flex flex-col gap-0.5">
                                {displayChapters.map((chapter) => {
                                    const isCurrent = chapter === currentChapter;
                                    return (
                                        <li key={chapter.startSec}>
                                            <button
                                                type="button"
                                                disabled={!onTimestampClick}
                                                onClick={() => onTimestampClick?.(chapter.startSec)}
                                                className={`-mx-2 flex w-[calc(100%+1rem)] items-baseline gap-3 rounded-md px-2 py-1 text-left text-sm transition-colors ${
                                                    onTimestampClick ? 'cursor-pointer hover:bg-foreground/5' : ''
                                                } ${isCurrent ? 'font-semibold' : ''}`}
                                            >
                                                <span className="flex-shrink-0 tabular-nums text-link">
                                                    {formatElapsed(chapter.startSec)}
                                                </span>
                                                <span className="min-w-0 truncate">{chapter.label}</span>
                                            </button>
                                        </li>
                                    );
                                })}
                            </ul>
                        </div>
                    )}
                </div>
            </div>

            <VideoFormDialog
                open={isDialogOpen}
                onOpenChange={setIsDialogOpen}
                video={adminVideo}
                onSubmit={handleDialogSubmit}
                isSubmitting={updateMutation.isPending}
            />
        </>
    );
}
