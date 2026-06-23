"use client";

import { useState } from "react";
import Link from "next/link";
import { useQueryState, parseAsInteger } from "nuqs";
import { toast } from "sonner";
import { Trash2, MoreVertical } from "lucide-react";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { VideoPreview } from "@/components/video-preview";
import { CustomPagination } from "@/components/common/custom-pagination";
import {
    useGetWatchHistories,
    useDeleteWatchHistory,
    useDeleteAllWatchHistories,
} from "@/lib/api/endpoints/watch-history/watch-history";
import type { WatchHistorySearchResponse } from "@/lib/api/models";
import { useQueryClient } from "@tanstack/react-query";

const PAGE_SIZE = 20;

// duration(초)을 HH:MM:SS 형식으로 변환
function formatDuration(seconds: number): string {
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    const secs = seconds % 60;

    if (hours > 0) {
        return `${hours}:${minutes.toString().padStart(2, "0")}:${secs.toString().padStart(2, "0")}`;
    }
    return `${minutes}:${secs.toString().padStart(2, "0")}`;
}

// watchedAt을 "n시간 전" 형식으로 변환
function formatTimeAgo(dateString: string): string {
    const now = new Date();
    const watchedAt = new Date(dateString);
    const diffInSeconds = Math.floor((now.getTime() - watchedAt.getTime()) / 1000);

    const intervals = [
        { label: "년", seconds: 31536000 },
        { label: "개월", seconds: 2592000 },
        { label: "일", seconds: 86400 },
        { label: "시간", seconds: 3600 },
        { label: "분", seconds: 60 },
    ];

    for (const interval of intervals) {
        const count = Math.floor(diffInSeconds / interval.seconds);
        if (count >= 1) {
            return `${count}${interval.label} 전`;
        }
    }

    return "방금 전";
}

export function WatchHistoryList() {
    const queryClient = useQueryClient();
    // URL은 1-based, API는 0-based
    const [urlPage] = useQueryState("page", parseAsInteger.withDefault(1));
    const page = Math.max(0, urlPage - 1);

    const params = { pageable: { page, size: PAGE_SIZE } };

    // 개인화 데이터라 SSR 프리페치 이득이 작고, SSR 단계에서는 토큰 자동 갱신이
    // 동작하지 않아 빈 결과로 굳을 수 있다. 클라이언트에서 직접 조회한다.
    const { data, isLoading } = useGetWatchHistories(params, {
        query: {
            placeholderData: (prev) => prev,
        },
    });

    const histories = data?.content ?? [];
    const totalPages = data?.totalPages ?? 0;

    const invalidate = () => {
        queryClient.invalidateQueries({ queryKey: ["/watch-histories"] });
    };

    const deleteOne = useDeleteWatchHistory({
        mutation: {
            onSuccess: () => {
                toast.success("시청 기록에서 삭제되었습니다.");
                invalidate();
            },
            onError: () => toast.error("삭제에 실패했습니다."),
        },
    });

    const deleteAll = useDeleteAllWatchHistories({
        mutation: {
            onSuccess: () => {
                toast.success("시청 기록을 모두 삭제했습니다.");
                invalidate();
            },
            onError: () => toast.error("삭제에 실패했습니다."),
        },
    });

    const handleDeleteOne = (item: WatchHistorySearchResponse) => {
        deleteOne.mutate({ videoUuid: item.video.uuid });
    };

    const handleDeleteAll = () => {
        if (!confirm("시청 기록을 모두 삭제하시겠습니까?")) return;
        deleteAll.mutate();
    };

    return (
        <>
            <div className="flex items-center justify-between mb-4">
                <h1 className="text-xl font-semibold">시청 기록</h1>
                {histories.length > 0 && (
                    <Button
                        variant="outline"
                        onClick={handleDeleteAll}
                        disabled={deleteAll.isPending}
                    >
                        <Trash2 className="w-4 h-4" />
                        전체 삭제
                    </Button>
                )}
            </div>

            {isLoading ? (
                <div className="text-center py-12">
                    <p className="text-muted-foreground">불러오는 중...</p>
                </div>
            ) : histories.length === 0 ? (
                <div className="text-center py-12">
                    <p className="text-muted-foreground">시청 기록이 없습니다.</p>
                </div>
            ) : (
                <>
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
                        {histories.map((item) => (
                            <WatchHistoryCard
                                key={item.video.uuid}
                                item={item}
                                onDelete={() => handleDeleteOne(item)}
                            />
                        ))}
                    </div>

                    <CustomPagination totalPages={totalPages} useShallowRouting />
                </>
            )}
        </>
    );
}

interface WatchHistoryCardProps {
    item: WatchHistorySearchResponse;
    onDelete: () => void;
}

function WatchHistoryCard({ item, onDelete }: WatchHistoryCardProps) {
    const { video, channel, lastPosition, progress, watchedAt } = item;
    const [isHovered, setIsHovered] = useState(false);

    // 이어보기 위치(초)를 쿼리로 넘겨 정확한 지점부터 재생
    const href = `/videos/${video.uuid}?t=${Math.floor(lastPosition)}`;
    // 진행률은 0~100 범위로 클램프
    const clampedProgress = Math.min(100, Math.max(0, progress));

    return (
        <div
            className="relative flex flex-col gap-3 mb-4 md:mb-5"
            onMouseEnter={() => setIsHovered(true)}
            onMouseLeave={() => setIsHovered(false)}
        >
            <Link href={href} className="absolute inset-0 z-10" aria-label={video.title} />

            <div className="relative aspect-video rounded-lg overflow-hidden">
                <VideoPreview
                    thumbnailUrl={video.thumbnailUrl}
                    playlistUrl={video.playlistUrl}
                    title={video.title}
                    isHovered={isHovered}
                />

                {/* 영상 길이 */}
                <span className="absolute right-2 bottom-2 rounded bg-black/70 px-1.5 py-0.5 text-xs text-white">
                    {formatDuration(video.duration)}
                </span>

                {/* 이어보기 진행률 바 */}
                <div className="absolute bottom-0 left-0 right-0 h-1 bg-white/30">
                    <div
                        className="h-full bg-red-600"
                        style={{ width: `${clampedProgress}%` }}
                    />
                </div>
            </div>

            <div className="flex gap-3">
                <Link href={`/channels/${channel.uuid}`} className="relative z-20">
                    <Avatar className="w-9 h-9">
                        <AvatarImage src={channel.profileUrl} />
                        <AvatarFallback>{channel.name[0]}</AvatarFallback>
                    </Avatar>
                </Link>
                <div className="flex flex-1 min-w-0 gap-1">
                    <div className="flex flex-col min-w-0">
                        <h3 className="text-md font-medium line-clamp-2">{video.title}</h3>
                        <Link
                            href={`/channels/${channel.uuid}`}
                            className="relative z-20 text-sm text-muted-foreground hover:text-foreground"
                        >
                            {channel.name}
                        </Link>
                        <p className="text-sm text-muted-foreground">{formatTimeAgo(watchedAt)} 시청</p>
                    </div>

                    {/* 더보기 메뉴 */}
                    <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                            <Button
                                type="button"
                                variant="ghost"
                                size="icon"
                                className="relative z-20 ml-auto -mr-2 h-8 w-8 shrink-0 text-muted-foreground"
                                aria-label="더보기"
                            >
                                <MoreVertical className="h-4 w-4" />
                            </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end">
                            <DropdownMenuItem onClick={onDelete}>
                                <Trash2 className="h-4 w-4" />
                                시청 기록에서 삭제
                            </DropdownMenuItem>
                        </DropdownMenuContent>
                    </DropdownMenu>
                </div>
            </div>
        </div>
    );
}
