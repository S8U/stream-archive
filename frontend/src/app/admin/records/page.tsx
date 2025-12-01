"use client";

import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Button } from "@/components/ui/button";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Loader2, Ban } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { useState } from "react";
import { CustomPagination } from "@/components/common/custom-pagination";
import { useSearchAdminRecords, useCancelAdminRecord } from "@/lib/api/endpoints/admin-record/admin-record";
import type { AdminRecordResponse, AdminRecordSearchRequestPlatformType } from "@/lib/api/models";
import { useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";

type SearchField = "channelName";
type RecordStatus = "__all__" | "recording" | "ended" | "cancelled";

export default function RecordsPage() {
    const queryClient = useQueryClient();

    // Search/Filter state
    const [searchField] = useState<SearchField>("channelName");
    const [searchQuery, setSearchQuery] = useState("");
    const [searchPlatform, setSearchPlatform] = useState<string>("__all__");
    const [searchStatus, setSearchStatus] = useState<RecordStatus>("__all__");

    // Pagination state
    const [page, setPage] = useState(0);
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
            channelName: searchQuery || undefined,
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

    const getPlatformBadgeVariant = (platform: string): "default" | "secondary" | "outline" | "destructive" => {
        switch (platform) {
            case "CHZZK":
                return "default"; // Green-ish usually, but default is fine
            case "TWITCH":
                return "secondary"; // Purple-ish usually
            case "SOOP":
                return "outline"; // Blue-ish usually
            default:
                return "outline";
        }
    };

    const getPlatformLabel = (platform: string) => {
        switch (platform) {
            case "CHZZK":
                return "치지직";
            case "TWITCH":
                return "트위치";
            case "SOOP":
                return "숲(아프리카)";
            default:
                return platform;
        }
    };

    const getStatusBadge = (record: AdminRecordResponse) => {
        if (record.isCancelled) {
            return <Badge variant="destructive">취소됨</Badge>;
        }
        if (record.isEnded) {
            return <Badge variant="secondary">종료됨</Badge>;
        }
        return <Badge variant="default" className="animate-pulse">녹화중</Badge>;
    };

    return (
        <div className="min-w-0">
            <h2 className="text-2xl font-bold">녹화 관리</h2>
            <p className="text-muted-foreground">진행 중이거나 종료된 녹화 기록을 관리합니다.</p>

            <div className="flex flex-col gap-4 mt-6 lg:flex-row lg:items-center lg:justify-between">
                {/* 왼쪽: 검색 및 필터 영역 */}
                <div className="flex flex-col gap-2 sm:flex-row sm:flex-wrap">
                    <Select value={searchPlatform} onValueChange={setSearchPlatform}>
                        <SelectTrigger className="w-full sm:w-auto sm:min-w-[120px]">
                            <span className="text-muted-foreground">플랫폼:</span>
                            <SelectValue placeholder="전체" />
                        </SelectTrigger>
                        <SelectContent>
                            <SelectGroup>
                                <SelectItem value="__all__">전체</SelectItem>
                                <SelectItem value="CHZZK">치지직</SelectItem>
                                <SelectItem value="SOOP">숲(아프리카)</SelectItem>
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
                        placeholder="채널명 검색"
                        className="w-full sm:flex-1 sm:min-w-[200px]"
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
                            <TableHead className="border-r font-semibold w-[100px] text-center">플랫폼</TableHead>
                            <TableHead className="border-r font-semibold w-[100px] text-center">채널 ID</TableHead>
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
                        ) : !recordsData?.content || recordsData.content.length === 0 ? (
                            <TableRow>
                                <TableCell colSpan={9} className="text-center py-8 text-muted-foreground">
                                    녹화 기록이 없습니다.
                                </TableCell>
                            </TableRow>
                        ) : (
                            recordsData.content.map((record) => (
                                <TableRow key={record.id}>
                                    <TableCell className="border-r text-center">{record.id}</TableCell>
                                    <TableCell className="border-r text-center">
                                        <Badge variant={getPlatformBadgeVariant(record.platformType)}>
                                            {getPlatformLabel(record.platformType)}
                                        </Badge>
                                    </TableCell>
                                    <TableCell className="border-r text-center">
                                        {record.channelId}
                                    </TableCell>
                                    <TableCell className="border-r font-mono text-xs">
                                        {record.platformStreamId}
                                    </TableCell>
                                    <TableCell className="border-r text-center text-sm">{record.recordQuality}</TableCell>
                                    <TableCell className="border-r text-center">
                                        {getStatusBadge(record)}
                                    </TableCell>
                                    <TableCell className="border-r text-center text-sm">{formatDate(record.createdAt)}</TableCell>
                                    <TableCell className="border-r text-center text-sm">
                                        {record.endedAt ? formatDate(record.endedAt) : "-"}
                                    </TableCell>
                                    <TableCell className="text-center">
                                        {!record.isEnded && !record.isCancelled && (
                                            <Button
                                                variant="destructive"
                                                size="icon"
                                                onClick={() => handleCancel(record)}
                                                disabled={cancelMutation.isPending}
                                                title="녹화 중단"
                                            >
                                                <Ban className="h-4 w-4" />
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