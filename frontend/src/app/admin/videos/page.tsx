"use client";

import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import { Switch } from "@/components/ui/switch";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Bookmark, Edit, Loader2, Trash2 } from "lucide-react";
import { AdminBadge, getPrivacyBadgeTone, getPrivacyLabel } from "@/components/common/admin-badge";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { SortableTableHead } from "@/components/common/sortable-table-head";
import { useEffect, useState } from "react";
import { useQueryState, parseAsBoolean, parseAsInteger, parseAsStringLiteral } from "nuqs";
import { useTableSort } from "@/hooks/use-table-sort";
import { CustomPagination } from "@/components/common/custom-pagination";
import { VideoFormDialog } from "@/components/admin/video-form-dialog";
import { useSearchAdminVideos, useSetArchivedAdminVideo, useUpdateAdminVideo, useDeleteAdminVideo } from "@/lib/api/endpoints/video-admin/video-admin";
import type { VideoAdminSearchResponse, VideoAdminSearchRequestContentPrivacy, VideoAdminUpdateRequestContentPrivacy } from "@/lib/api/models";
import { useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import Link from "next/link";

const searchFieldOptions = ["id", "uuid", "channelName", "title", "description"] as const;
const privacyOptions = ["__none__", "PUBLIC", "UNLISTED", "PRIVATE"] as const;
const sortFieldOptions = [
    "id",
    "channelName",
    "title",
    "uuid",
    "duration",
    "fileSize",
    "contentPrivacy",
    "isArchived",
    "createdAt",
] as const;

export default function VideosPage() {
    const queryClient = useQueryClient();

    // Dialog state
    const [isDialogOpen, setIsDialogOpen] = useState(false);
    const [selectedVideo, setSelectedVideo] = useState<VideoAdminSearchResponse | null>(null);

    // URL 상태 (nuqs)
    const [searchField, setSearchField] = useQueryState("field", parseAsStringLiteral(searchFieldOptions).withDefault("title"));
    const [searchQuery, setSearchQuery] = useQueryState("q", { defaultValue: "" });
    const [searchContentPrivacy, setSearchContentPrivacy] = useQueryState("privacy", parseAsStringLiteral(privacyOptions).withDefault("__none__"));
    const [searchArchived, setSearchArchived] = useQueryState("archived", parseAsBoolean.withDefault(false));
    const [page, setPage] = useQueryState("page", parseAsInteger.withDefault(1));
    const { sortField, sortDirection, sortParam, handleSort, resetSort } = useTableSort(sortFieldOptions, setPage);
    const [draftSearchField, setDraftSearchField] = useState(searchField);
    const [draftSearchQuery, setDraftSearchQuery] = useState(searchQuery);
    const [draftSearchContentPrivacy, setDraftSearchContentPrivacy] = useState(searchContentPrivacy);
    const [draftSearchArchived, setDraftSearchArchived] = useState(searchArchived);

    const size = 10;

    useEffect(() => {
        setDraftSearchField(searchField);
        setDraftSearchQuery(searchQuery);
        setDraftSearchContentPrivacy(searchContentPrivacy);
        setDraftSearchArchived(searchArchived);
    }, [searchField, searchQuery, searchContentPrivacy, searchArchived]);

    // Build search params
    const searchParams = {
        request: {
            id: searchField === "id" && searchQuery ? Number(searchQuery) : undefined,
            uuid: searchField === "uuid" ? searchQuery : undefined,
            channelName: searchField === "channelName" ? searchQuery : undefined,
            title: searchField === "title" ? searchQuery : undefined,
            description: searchField === "description" ? searchQuery : undefined,
            contentPrivacy: searchContentPrivacy !== "__none__" ? (searchContentPrivacy as VideoAdminSearchRequestContentPrivacy) : undefined,
            isArchived: searchArchived ? true : undefined,
        },
        pageable: {
            page: page - 1,
            size,
            sort: [sortParam],
        },
    };

    // API Hooks
    const { data: videosData, isLoading, error } = useSearchAdminVideos(searchParams);
    const updateMutation = useUpdateAdminVideo();
    const deleteMutation = useDeleteAdminVideo();
    const archiveMutation = useSetArchivedAdminVideo();

    // Handlers
    const handleSearch = () => {
        setSearchField(draftSearchField);
        setSearchQuery(draftSearchQuery);
        setSearchContentPrivacy(draftSearchContentPrivacy);
        setSearchArchived(draftSearchArchived);
        setPage(1);
    };

    const handleReset = () => {
        setDraftSearchField("title");
        setDraftSearchQuery("");
        setDraftSearchContentPrivacy("__none__");
        setDraftSearchArchived(false);
        setSearchField("title");
        setSearchQuery("");
        setSearchContentPrivacy("__none__");
        setSearchArchived(false);
        resetSort();
        setPage(1);
    };

    const handleOpenEditDialog = (video: VideoAdminSearchResponse) => {
        setSelectedVideo(video);
        setIsDialogOpen(true);
    };

    const handleDialogClose = () => {
        setIsDialogOpen(false);
        setSelectedVideo(null);
    };

    const handleDialogSubmit = async (data: { title: string; description: string; contentPrivacy: VideoAdminUpdateRequestContentPrivacy; chatSyncOffsetMillis: number }) => {
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

    const handleToggleArchive = async (video: VideoAdminSearchResponse, checked: boolean) => {
        try {
            await archiveMutation.mutateAsync({ id: video.id, data: { isArchived: checked } });
            toast.success(`"${video.title}" 동영상이 ${checked ? "소장" : "소장 해제"}되었습니다.`);
            queryClient.invalidateQueries({ queryKey: ["/admin/videos"] });
        } catch {
            toast.error("처리에 실패했습니다.");
        }
    };

    const handleDelete = async (video: VideoAdminSearchResponse) => {
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
                    <Select value={draftSearchField} onValueChange={(value) => setDraftSearchField(value as typeof searchFieldOptions[number])}>
                        <SelectTrigger className="w-full sm:w-auto sm:min-w-[120px]">
                            <div className="flex w-full justify-between gap-2">
                                <span className="text-muted-foreground">검색 기준:</span>
                                <SelectValue placeholder="검색 기준" />
                            </div>
                        </SelectTrigger>
                        <SelectContent>
                            <SelectGroup>
                                <SelectItem value="id">ID</SelectItem>
                                <SelectItem value="uuid">UUID</SelectItem>
                                <SelectItem value="channelName">채널명</SelectItem>
                                <SelectItem value="title">제목</SelectItem>
                                <SelectItem value="description">설명</SelectItem>
                            </SelectGroup>
                        </SelectContent>
                    </Select>
                    <Select value={draftSearchContentPrivacy} onValueChange={(value) => setDraftSearchContentPrivacy(value as typeof privacyOptions[number])}>
                        <SelectTrigger className="w-full sm:w-auto sm:min-w-[128px]">
                            <div className="flex w-full justify-between gap-2">
                                <span className="text-muted-foreground">공개 범위:</span>
                                <SelectValue placeholder="공개 범위" />
                            </div>
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
                        value={draftSearchQuery}
                        onChange={(e) => setDraftSearchQuery(e.target.value)}
                        onKeyDown={(e) => e.key === "Enter" && handleSearch()}
                    />
                    <label className="flex items-center gap-2 px-2">
                        <Checkbox
                            checked={draftSearchArchived}
                            onCheckedChange={(v) => setDraftSearchArchived(v === true)}
                        />
                        <span className="text-sm whitespace-nowrap">소장만 보기</span>
                    </label>
                    <Button variant="default" onClick={handleSearch}>검색</Button>
                    <Button variant="outline" onClick={handleReset}>초기화</Button>
                </div>
            </div>

            {/* 동영상 목록 */}
            <div className="w-full rounded-lg border mt-4 overflow-x-auto">
                <Table className="w-full">
                    <TableHeader className="bg-muted">
                        <TableRow>
                            <SortableTableHead
                                className="border-r font-semibold w-[60px] text-center"
                                field="id"
                                currentField={sortField}
                                currentDirection={sortDirection}
                                onSort={handleSort}
                                align="center"
                            >
                                ID
                            </SortableTableHead>
                            <SortableTableHead
                                className="border-r font-semibold"
                                field="channelName"
                                currentField={sortField}
                                currentDirection={sortDirection}
                                onSort={handleSort}
                            >
                                채널 정보
                            </SortableTableHead>
                            <SortableTableHead
                                className="border-r font-semibold"
                                field="title"
                                currentField={sortField}
                                currentDirection={sortDirection}
                                onSort={handleSort}
                            >
                                동영상 정보
                            </SortableTableHead>
                            <SortableTableHead
                                className="border-r font-semibold w-[350px]"
                                field="uuid"
                                currentField={sortField}
                                currentDirection={sortDirection}
                                onSort={handleSort}
                            >
                                UUID
                            </SortableTableHead>
                            <SortableTableHead
                                className="border-r font-semibold w-[100px] text-center"
                                field="duration"
                                currentField={sortField}
                                currentDirection={sortDirection}
                                onSort={handleSort}
                                align="center"
                            >
                                길이
                            </SortableTableHead>
                            <SortableTableHead
                                className="border-r font-semibold w-[100px] text-center"
                                field="fileSize"
                                currentField={sortField}
                                currentDirection={sortDirection}
                                onSort={handleSort}
                                align="center"
                            >
                                용량
                            </SortableTableHead>
                            <SortableTableHead
                                className="border-r font-semibold w-[100px] text-center"
                                field="contentPrivacy"
                                currentField={sortField}
                                currentDirection={sortDirection}
                                onSort={handleSort}
                                align="center"
                            >
                                공개 범위
                            </SortableTableHead>
                            <SortableTableHead
                                className="border-r font-semibold w-[80px] text-center"
                                field="isArchived"
                                currentField={sortField}
                                currentDirection={sortDirection}
                                onSort={handleSort}
                                align="center"
                            >
                                소장
                            </SortableTableHead>
                            <SortableTableHead
                                className="border-r font-semibold w-[120px] text-center"
                                field="createdAt"
                                currentField={sortField}
                                currentDirection={sortDirection}
                                onSort={handleSort}
                                align="center"
                            >
                                생성일
                            </SortableTableHead>
                            <TableHead className="font-semibold w-[100px] text-center">작업</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {isLoading ? (
                            <TableRow>
                                <TableCell colSpan={10} className="text-center py-8">
                                    <Loader2 className="h-6 w-6 animate-spin mx-auto" />
                                </TableCell>
                            </TableRow>
                        ) : error ? (
                            <TableRow>
                                <TableCell colSpan={10} className="text-center py-8 text-destructive">
                                    데이터를 불러오는 중 오류가 발생했습니다.
                                </TableCell>
                            </TableRow>
                        ) : !videosData?.content || videosData.content.length === 0 ? (
                            <TableRow>
                                <TableCell colSpan={10} className="text-center py-8 text-muted-foreground">
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
                                                className="hover:underline truncate max-w-[300px] inline-flex items-center gap-1"
                                            >
                                                <span className="truncate">{video.title}</span>
                                                {video.isArchived && (
                                                    <Bookmark size={13} className="flex-shrink-0 text-muted-foreground opacity-50" fill="currentColor" />
                                                )}
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
                                        <AdminBadge tone={getPrivacyBadgeTone(video.contentPrivacy)}>
                                            {getPrivacyLabel(video.contentPrivacy)}
                                        </AdminBadge>
                                    </TableCell>

                                    {/* 소장 */}
                                    <TableCell className="border-r text-center">
                                        <Switch
                                            checked={video.isArchived}
                                            onCheckedChange={(checked) => handleToggleArchive(video, checked)}
                                            disabled={archiveMutation.isPending}
                                        />
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
