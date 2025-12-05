"use client";

import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Button } from "@/components/ui/button";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Edit, Loader2, Trash2 } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { useState } from "react";
import { useQueryState, parseAsInteger, parseAsStringLiteral } from "nuqs";
import { CustomPagination } from "@/components/common/custom-pagination";
import { VideoFormDialog } from "@/components/admin/video-form-dialog";
import { useSearchAdminVideos, useUpdateAdminVideo, useDeleteAdminVideo } from "@/lib/api/endpoints/admin-video/admin-video";
import type { AdminVideoResponse, AdminVideoSearchRequestContentPrivacy, AdminVideoUpdateRequestContentPrivacy } from "@/lib/api/models";
import { useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import Link from "next/link";

const searchFieldOptions = ["title", "channelName"] as const;
const privacyOptions = ["__none__", "PUBLIC", "UNLISTED", "PRIVATE"] as const;

export default function VideosPage() {
    const queryClient = useQueryClient();

    // Dialog state
    const [isDialogOpen, setIsDialogOpen] = useState(false);
    const [selectedVideo, setSelectedVideo] = useState<AdminVideoResponse | null>(null);

    // URL 상태 (nuqs)
    const [searchField, setSearchField] = useQueryState("field", parseAsStringLiteral(searchFieldOptions).withDefault("title"));
    const [searchQuery, setSearchQuery] = useQueryState("q", { defaultValue: "" });
    const [searchContentPrivacy, setSearchContentPrivacy] = useQueryState("privacy", parseAsStringLiteral(privacyOptions).withDefault("__none__"));
    const [page, setPage] = useQueryState("page", parseAsInteger.withDefault(1));

    const size = 10;

    // Build search params
    const searchParams = {
        request: {
            title: searchField === "title" ? searchQuery : undefined,
            channelName: searchField === "channelName" ? searchQuery : undefined,
            contentPrivacy: searchContentPrivacy !== "__none__" ? (searchContentPrivacy as AdminVideoSearchRequestContentPrivacy) : undefined,
        },
        pageable: {
            page: page - 1,
            size,
        },
    };

    // API Hooks
    const { data: videosData, isLoading, error } = useSearchAdminVideos(searchParams);
    const updateMutation = useUpdateAdminVideo();
    const deleteMutation = useDeleteAdminVideo();

    // Handlers
    const handleSearch = () => {
        setPage(1);
    };

    const handleReset = () => {
        setSearchField("title");
        setSearchQuery("");
        setSearchContentPrivacy("__none__");
        setPage(1);
    };

    const handleOpenEditDialog = (video: AdminVideoResponse) => {
        setSelectedVideo(video);
        setIsDialogOpen(true);
    };

    const handleDialogClose = () => {
        setIsDialogOpen(false);
        setSelectedVideo(null);
    };

    const handleDialogSubmit = async (data: { title: string; contentPrivacy: AdminVideoUpdateRequestContentPrivacy }) => {
        if (!selectedVideo) return;

        try {
            await updateMutation.mutateAsync({
                id: selectedVideo.id,
                data,
            });
            toast.success(`"${data.title}" 동영상이 수정되었습니다.`);

            // Invalidate and refetch
            queryClient.invalidateQueries({ queryKey: ["/admin/videos"] });
            handleDialogClose();
        } catch (error) {
            toast.error("동영상 수정에 실패했습니다.");
        }
    };

    const handleDelete = async (video: AdminVideoResponse) => {
        if (!confirm(`"${video.title}" 동영상을 삭제하시겠습니까?`)) {
            return;
        }

        try {
            await deleteMutation.mutateAsync({ id: video.id });
            toast.success(`"${video.title}" 동영상이 삭제되었습니다.`);
            queryClient.invalidateQueries({ queryKey: ["/admin/videos"] });
        } catch (error) {
            toast.error("동영상 삭제에 실패했습니다.");
        }
    };

    const formatDate = (dateString: string) => {
        return new Date(dateString).toLocaleDateString("ko-KR");
    };

    const getPrivacyLabel = (privacy: string) => {
        switch (privacy) {
            case "PUBLIC":
                return "공개";
            case "UNLISTED":
                return "일부공개";
            case "PRIVATE":
                return "비공개";
            default:
                return privacy;
        }
    };

    const getPrivacyVariant = (privacy: string): "default" | "secondary" | "outline" => {
        switch (privacy) {
            case "PUBLIC":
                return "secondary";
            case "UNLISTED":
                return "outline";
            case "PRIVATE":
                return "outline";
            default:
                return "default";
        }
    };

    const formatDuration = (seconds: number) => {
        const h = Math.floor(seconds / 3600);
        const m = Math.floor((seconds % 3600) / 60);
        const s = seconds % 60;
        return `${h > 0 ? `${h}:` : ""}${m.toString().padStart(2, "0")}:${s.toString().padStart(2, "0")}`;
    };

    const formatFileSize = (bytes: number) => {
        if (bytes === 0) return "0 B";
        const k = 1024;
        const sizes = ["B", "KB", "MB", "GB", "TB"];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + " " + sizes[i];
    };

    return (
        <div className="min-w-0">
            <h2 className="text-2xl font-bold">동영상 관리</h2>
            <p className="text-muted-foreground">등록된 동영상 목록을 관리합니다.</p>

            <div className="flex flex-col gap-4 mt-6 lg:flex-row lg:items-center lg:justify-between">
                {/* 왼쪽: 검색 및 필터 영역 */}
                <div className="flex flex-col gap-2 sm:flex-row sm:flex-wrap">
                    <Select value={searchField} onValueChange={(value) => setSearchField(value as typeof searchFieldOptions[number])}>
                        <SelectTrigger className="w-full sm:w-auto sm:min-w-[120px]">
                            <span className="text-muted-foreground">검색 기준:</span>
                            <SelectValue placeholder="검색 기준" />
                        </SelectTrigger>
                        <SelectContent>
                            <SelectGroup>
                                <SelectItem value="title">제목</SelectItem>
                                <SelectItem value="channelName">채널명</SelectItem>
                            </SelectGroup>
                        </SelectContent>
                    </Select>
                    <Select value={searchContentPrivacy} onValueChange={(value) => setSearchContentPrivacy(value as typeof privacyOptions[number])}>
                        <SelectTrigger className="w-full sm:w-auto sm:min-w-[128px]">
                            <span className="text-muted-foreground">공개 범위:</span>
                            <SelectValue placeholder="공개 범위" />
                        </SelectTrigger>
                        <SelectContent>
                            <SelectGroup>
                                <SelectItem value="__none__">전체</SelectItem>
                                <SelectItem value="PUBLIC">공개</SelectItem>
                                <SelectItem value="UNLISTED">일부공개</SelectItem>
                                <SelectItem value="PRIVATE">비공개</SelectItem>
                            </SelectGroup>
                        </SelectContent>
                    </Select>
                    <Input
                        type="text"
                        placeholder="검색어 입력"
                        className="w-full sm:flex-1 sm:min-w-[300px]"
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                    />
                    <Button variant="default" onClick={handleSearch}>검색</Button>
                    <Button variant="outline" onClick={handleReset}>초기화</Button>
                </div>
            </div>

            {/* 동영상 목록 */}
            <div className="w-full rounded-lg border mt-4 overflow-x-auto">
                <Table className="w-full">
                    <TableHeader className="bg-muted">
                        <TableRow>
                            <TableHead className="border-r font-semibold w-[60px] text-center">ID</TableHead>
                            <TableHead className="border-r font-semibold">채널 정보</TableHead>
                            <TableHead className="border-r font-semibold">동영상 정보</TableHead>
                            <TableHead className="border-r font-semibold w-[350px]">UUID</TableHead>
                            <TableHead className="border-r font-semibold w-[100px] text-center">길이</TableHead>
                            <TableHead className="border-r font-semibold w-[100px] text-center">용량</TableHead>
                            <TableHead className="border-r font-semibold w-[100px] text-center">공개 범위</TableHead>
                            <TableHead className="border-r font-semibold w-[120px] text-center">생성일</TableHead>
                            <TableHead className="font-semibold w-[100px] text-center">작업</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {isLoading ? (
                            <TableRow>
                                <TableCell colSpan={9} className="text-center py-8">
                                    <Loader2 className="h-6 w-6 animate-spin mx-auto" />
                                </TableCell>
                            </TableRow>
                        ) : error ? (
                            <TableRow>
                                <TableCell colSpan={9} className="text-center py-8 text-destructive">
                                    데이터를 불러오는 중 오류가 발생했습니다.
                                </TableCell>
                            </TableRow>
                        ) : !videosData?.content || videosData.content.length === 0 ? (
                            <TableRow>
                                <TableCell colSpan={9} className="text-center py-8 text-muted-foreground">
                                    등록된 동영상이 없습니다.
                                </TableCell>
                            </TableRow>
                        ) : (
                            videosData.content.map((video) => (
                                <TableRow key={video.id}>
                                    {/* ID */}
                                    <TableCell className="border-r text-center">{video.id}</TableCell>

                                    {/* 채널 정보 */}
                                    <TableCell className="border-r">
                                        <div className="flex items-center gap-2">
                                            <Avatar className="h-8 w-8">
                                                <AvatarImage src={video.channel.profileUrl} />
                                                <AvatarFallback>{video.channel.name[0]?.toUpperCase()}</AvatarFallback>
                                            </Avatar>
                                            <span>{video.channel.name}</span>
                                        </div>
                                    </TableCell>

                                    {/* 동영상 정보 (썸네일 + 제목) */}
                                    <TableCell className="border-r">
                                        <div className="flex items-center gap-3">
                                            <div className="relative w-16 h-9 bg-muted rounded overflow-hidden flex-shrink-0">
                                                {video.thumbnailUrl ? (
                                                    <img src={video.thumbnailUrl} alt={video.title} className="w-full h-full object-cover" />
                                                ) : (
                                                    <div className="w-full h-full flex items-center justify-center text-xs text-muted-foreground">No Img</div>
                                                )}
                                            </div>
                                            <Link
                                                href={`/videos/${video.uuid}`}
                                                target="_blank"
                                                rel="noopener noreferrer"
                                                className="hover:underline truncate"
                                            >
                                                {video.title}
                                            </Link>
                                        </div>
                                    </TableCell>

                                    {/* UUID */}
                                    <TableCell className="border-r font-mono text-xs">{video.uuid}</TableCell>

                                    {/* 길이 */}
                                    <TableCell className="border-r text-center">{formatDuration(video.duration)}</TableCell>

                                    {/* 용량 */}
                                    <TableCell className="border-r text-center">{formatFileSize(video.fileSize)}</TableCell>

                                    {/* 공개 범위 */}
                                    <TableCell className="border-r text-center">
                                        <Badge variant={getPrivacyVariant(video.contentPrivacy)}>
                                            {getPrivacyLabel(video.contentPrivacy)}
                                        </Badge>
                                    </TableCell>

                                    {/* 생성일 */}
                                    <TableCell className="border-r text-center">{formatDate(video.createdAt)}</TableCell>

                                    {/* 작업 (수정/삭제) */}
                                    <TableCell className="text-center">
                                        <div className="flex gap-2 justify-center">
                                            <Button
                                                variant="secondary"
                                                size="icon"
                                                onClick={() => handleOpenEditDialog(video)}
                                                disabled={updateMutation.isPending}
                                            >
                                                <Edit />
                                            </Button>
                                            <Button
                                                variant="secondary"
                                                size="icon"
                                                onClick={() => handleDelete(video)}
                                                disabled={deleteMutation.isPending}
                                            >
                                                <Trash2 />
                                            </Button>
                                        </div>
                                    </TableCell>
                                </TableRow>
                            ))
                        )}
                    </TableBody>
                </Table>
            </div>

            {/* Pagination */}
            {videosData && (
                <CustomPagination
                    page={page - 1}
                    totalPages={videosData.totalPages || 0}
                    onPageChange={(p) => setPage(p + 1)}
                />
            )}

            {/* 동영상 수정 다이얼로그 */}
            <VideoFormDialog
                open={isDialogOpen}
                onOpenChange={handleDialogClose}
                video={selectedVideo}
                onSubmit={handleDialogSubmit}
                isSubmitting={updateMutation.isPending}
            />
        </div>
    );
}