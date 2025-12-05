"use client";

import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Button } from "@/components/ui/button";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Ban, Loader2 } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { useState } from "react";
import { useSearchParams } from "next/navigation";
import { CustomPagination } from "@/components/common/custom-pagination";
import { useCancelAdminRecord, useSearchAdminRecords } from "@/lib/api/endpoints/admin-record/admin-record";
import type { AdminRecordResponse, AdminRecordSearchRequestPlatformType } from "@/lib/api/models";
import { useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { PlatformBadge } from "@/components/common/platform-badge";
import Link from "next/link";

type SearchField = "channelName";
type RecordStatus = "__all__" | "recording" | "ended" | "cancelled";

export default function RecordsPage() {
    const queryClient = useQueryClient();
    const urlSearchParams = useSearchParams();

    // Search/Filter state
    const [searchField, setSearchField] = useState<SearchField>("channelName");
    const [searchQuery, setSearchQuery] = useState("");
    const [searchPlatform, setSearchPlatform] = useState<string>("__all__");
    const [searchStatus, setSearchStatus] = useState<RecordStatus>("__all__");

    // Pagination state - URL에서 초기값 읽기
    const initialPage = Math.max(0, Number(urlSearchParams.get("page") || 1) - 1);
    const [page, setPage] = useState(initialPage);
    const [size] = useState(10);

    // Build search params
    const getStatusParams = (status: RecordStatus) => {
        switch (status) {
            case "recording":
                return { isEnded: false, isCancelled: false };
            case "ended":
                return { isEnded: true, isCancelled: false };
            case "cancelled":
                return { isCancelled: true };
            default:
                return {};
        }
    };

    const searchParams = {
        request: {
            channelName: searchField === "channelName" ? searchQuery : undefined,
            platformType: searchPlatform !== "__all__" ? (searchPlatform as AdminRecordSearchRequestPlatformType) : undefined,
            ...getStatusParams(searchStatus),
        },
        pageable: {
            page,
            size,
        },
    };

    // API Hooks
    const { data: recordsData, isLoading, error } = useSearchAdminRecords(searchParams);
    const cancelMutation = useCancelAdminRecord();

    // Handlers
    const handleSearch = () => {
        setPage(0);
    };

    const handleReset = () => {
        setSearchField("channelName");
        setSearchQuery("");
        setSearchPlatform("__all__");
        setSearchStatus("__all__");
        setPage(0);
    };

    const handleCancel = async (record: AdminRecordResponse) => {
        if (!confirm(`녹화(ID: ${record.id})를 중단하시겠습니까?`)) {
            return;
        }

        try {
            await cancelMutation.mutateAsync({ id: record.id });
            toast.success(`녹화(ID: ${record.id})가 중단되었습니다.`);
            queryClient.invalidateQueries({ queryKey: ["/admin/records"] });
        } catch (error) {
            toast.error("녹화 중단에 실패했습니다.");
        }
    };

    const formatDate = (dateString: string) => {
        return new Date(dateString).toLocaleString("ko-KR");
    };

    const getStatusBadge = (record: AdminRecordResponse) => {
        if (record.isCancelled) {
            return <Badge variant="outline">취소됨</Badge>;
        }
        if (record.isEnded) {
            return <Badge variant="secondary">종료됨</Badge>;
        }
        return <Badge className="bg-red-100 text-red-700 hover:bg-red-100/80">녹화중</Badge>;
    };

    return (
        <div className="min-w-0">
            <h2 className="text-2xl font-bold">녹화 관리</h2>
            <p className="text-muted-foreground">진행 중이거나 종료된 녹화 기록을 관리합니다.</p>

            <div className="flex flex-col gap-4 mt-6 lg:flex-row lg:items-center lg:justify-between">
                {/* 왼쪽: 검색 및 필터 영역 */}
                <div className="flex flex-col gap-2 sm:flex-row sm:flex-wrap">
                    <Select value={searchField} onValueChange={(value) => setSearchField(value as SearchField)}>
                        <SelectTrigger className="w-full sm:w-auto sm:min-w-[120px]">
                            <span className="text-muted-foreground">검색 기준:</span>
                            <SelectValue placeholder="검색 기준" />
                        </SelectTrigger>
                        <SelectContent>
                            <SelectGroup>
                                <SelectItem value="channelName">채널명</SelectItem>
                            </SelectGroup>
                        </SelectContent>
                    </Select>
                    <Select value={searchPlatform} onValueChange={setSearchPlatform}>
                        <SelectTrigger className="w-full sm:w-auto sm:min-w-[120px]">
                            <span className="text-muted-foreground">플랫폼:</span>
                            <SelectValue placeholder="전체" />
                        </SelectTrigger>
                        <SelectContent>
                            <SelectGroup>
                                <SelectItem value="__all__">전체</SelectItem>
                                <SelectItem value="CHZZK">치지직</SelectItem>
                                <SelectItem value="SOOP">SOOP</SelectItem>
                                <SelectItem value="TWITCH">트위치</SelectItem>
                            </SelectGroup>
                        </SelectContent>
                    </Select>
                    <Select value={searchStatus} onValueChange={(value) => setSearchStatus(value as RecordStatus)}>
                        <SelectTrigger className="w-full sm:w-auto sm:min-w-[120px]">
                            <span className="text-muted-foreground">상태:</span>
                            <SelectValue placeholder="전체" />
                        </SelectTrigger>
                        <SelectContent>
                            <SelectGroup>
                                <SelectItem value="__all__">전체</SelectItem>
                                <SelectItem value="recording">녹화중</SelectItem>
                                <SelectItem value="ended">종료됨</SelectItem>
                                <SelectItem value="cancelled">취소됨</SelectItem>
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

            {/* 녹화 목록 */}
            <div className="w-full rounded-lg border mt-4 overflow-x-auto">
                <Table className="w-full">
                    <TableHeader className="bg-muted">
                        <TableRow>
                            <TableHead className="border-r font-semibold w-[60px] text-center">ID</TableHead>
                            <TableHead className="border-r font-semibold">채널 정보</TableHead>
                            <TableHead className="border-r font-semibold w-[100px] text-center">플랫폼</TableHead>
                            <TableHead className="border-r font-semibold">동영상 정보</TableHead>
                            <TableHead className="border-r font-semibold">스트림 ID</TableHead>
                            <TableHead className="border-r font-semibold w-[80px] text-center">화질</TableHead>
                            <TableHead className="border-r font-semibold w-[80px] text-center">상태</TableHead>
                            <TableHead className="border-r font-semibold w-[180px] text-center">시작 시간</TableHead>
                            <TableHead className="border-r font-semibold w-[180px] text-center">종료 시간</TableHead>
                            <TableHead className="font-semibold w-[80px] text-center">작업</TableHead>
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
                        ) : !recordsData?.content || recordsData.content.length === 0 ? (
                            <TableRow>
                                <TableCell colSpan={10} className="text-center py-8 text-muted-foreground">
                                    녹화 기록이 없습니다.
                                </TableCell>
                            </TableRow>
                        ) : (
                            recordsData.content.map((record) => (
                                <TableRow key={record.id}>
                                    {/* ID */}
                                    <TableCell className="border-r text-center">{record.id}</TableCell>

                                    {/* 채널 정보 */}
                                    <TableCell className="border-r">
                                        <div className="flex items-center gap-2">
                                            <Avatar className="h-8 w-8">
                                                <AvatarImage src={record.channel.profileUrl} />
                                                <AvatarFallback>{record.channel.name[0]?.toUpperCase()}</AvatarFallback>
                                            </Avatar>
                                            <span>{record.channel.name}</span>
                                        </div>
                                    </TableCell>

                                    {/* 플랫폼 */}
                                    <TableCell className="border-r text-center">
                                        <PlatformBadge platform={record.platformType} />
                                    </TableCell>

                                    {/* 동영상 정보 */}
                                    <TableCell className="border-r">
                                        <div className="flex items-center gap-3">
                                            <div className="relative w-16 h-9 bg-muted rounded overflow-hidden flex-shrink-0">
                                                {record.video.thumbnailUrl ? (
                                                    <img src={record.video.thumbnailUrl} alt={record.video.title} className="w-full h-full object-cover" />
                                                ) : (
                                                    <div className="w-full h-full flex items-center justify-center text-xs text-muted-foreground">No Img</div>
                                                )}
                                            </div>
                                            <Link
                                                href={`/videos/${record.video.uuid}`}
                                                target="_blank"
                                                rel="noopener noreferrer"
                                                className="hover:underline truncate"
                                            >
                                                {record.video.title}
                                            </Link>
                                        </div>
                                    </TableCell>

                                    {/* 스트림 ID */}
                                    <TableCell className="border-r font-mono text-xs">
                                        {record.platformStreamId}
                                    </TableCell>

                                    {/* 화질 */}
                                    <TableCell className="border-r text-center text-sm">{record.recordQuality}</TableCell>

                                    {/* 상태 */}
                                    <TableCell className="border-r text-center">
                                        {getStatusBadge(record)}
                                    </TableCell>

                                    {/* 시작 시간 */}
                                    <TableCell className="border-r text-center text-sm">{formatDate(record.createdAt)}</TableCell>

                                    {/* 종료 시간 */}
                                    <TableCell className="border-r text-center text-sm">
                                        {record.endedAt ? formatDate(record.endedAt) : "-"}
                                    </TableCell>

                                    {/* 작업 */}
                                    <TableCell className="text-center">
                                        {!record.isEnded && !record.isCancelled && (
                                            <Button
                                                variant="secondary"
                                                size="icon"
                                                onClick={() => handleCancel(record)}
                                                disabled={cancelMutation.isPending}
                                                title="녹화 중단"
                                            >
                                                <Ban />
                                            </Button>
                                        )}
                                    </TableCell>
                                </TableRow>
                            ))
                        )}
                    </TableBody>
                </Table>
            </div>

            {/* Pagination */}
            {recordsData && (
                <CustomPagination
                    page={page}
                    totalPages={recordsData.totalPages || 0}
                    onPageChange={setPage}
                />
            )}
        </div>
    );
}