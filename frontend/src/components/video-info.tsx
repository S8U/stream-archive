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
import {Bookmark, BookmarkX, ChevronDown, Edit, Loader2, MoreHorizontal, Trash2} from 'lucide-react';
import {useMemo, useState} from 'react';
import {toast} from 'sonner';
import {
    useDeleteAdminVideo,
    useSearchAdminVideos,
    useSetArchivedAdminVideo,
    useUpdateAdminVideo,
} from '@/lib/api/endpoints/admin-video/admin-video';
import {useGetChannelPlatforms} from '@/lib/api/endpoints/channel/channel';
import type {AdminVideoUpdateRequestContentPrivacy, PublicVideoResponse} from '@/lib/api/models';

interface VideoInfoProps {
    video: PublicVideoResponse;
    isAdmin?: boolean;
}

export function VideoInfo({ video, isAdmin = false }: VideoInfoProps) {
    const router = useRouter();
    const queryClient = useQueryClient();
    const [isExpanded, setIsExpanded] = useState(false);
    const [isDialogOpen, setIsDialogOpen] = useState(false);
    const recordedAt = video.record?.startedAt ?? video.createdAt;
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

    const stats = [
        { label: '녹화일', value: formatDateTime(recordedAt) },
        { label: '용량', value: formatFileSize(video.fileSize) },
        { label: '최고 시청자수', value: video.peakViewerCount != null ? video.peakViewerCount.toLocaleString('ko-KR') : '-' },
    ];

    const handleDialogSubmit = async (data: { title: string; description: string; contentPrivacy: AdminVideoUpdateRequestContentPrivacy; chatSyncOffsetMillis: number }) => {
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
                    variant="ghost"
                    size="icon"
                    className="-mr-2 h-9 w-9 flex-shrink-0"
                    aria-label="동영상 관리 메뉴"
                >
                    {isAdminVideoLoading ? (
                        <Loader2 className="h-4 w-4 animate-spin" />
                    ) : (
                        <MoreHorizontal className="h-5 w-5" />
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
            <div className="p-3">
                {/* 제목 */}
                <button
                    type="button"
                    className="mb-2 flex w-full items-start justify-between gap-3 text-left lg:pointer-events-none"
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
                </div>

                <div className={`${isExpanded ? 'block' : 'hidden'} relative mt-2 mb-4 rounded-md bg-muted px-3 py-3 lg:block`}>
                    {adminMenu && <div className="absolute top-1 right-3">{adminMenu}</div>}
                    {video.description && (
                        <p className="mb-3 pr-10 whitespace-pre-wrap text-sm leading-6">{video.description}</p>
                    )}
                    <dl className="mt-2 grid gap-x-4 gap-y-2 text-sm sm:grid-cols-2 lg:mt-0 xl:grid-cols-4">
                        {stats.map((stat) => (
                            <div key={stat.label} className="min-w-0">
                                <dt className="text-muted-foreground">{stat.label}</dt>
                                <dd className="mt-1 truncate font-medium">{stat.value}</dd>
                            </div>
                        ))}
                        <div className="min-w-0">
                            <dt className="text-muted-foreground">플랫폼</dt>
                            <dd className="mt-1">
                                {platformType ? (
                                    platformLink ? (
                                        <a href={platformLink} target="_blank" rel="noopener noreferrer" className="inline-flex">
                                            <PlatformBadge platform={platformType} />
                                        </a>
                                    ) : (
                                        <PlatformBadge platform={platformType} />
                                    )
                                ) : (
                                    <span className="font-medium">-</span>
                                )}
                            </dd>
                        </div>
                    </dl>
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
