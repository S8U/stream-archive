"use client";

import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Button } from "@/components/ui/button";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Edit, Loader2, Plus, Trash2 } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { useState } from "react";
import { CustomPagination } from "@/components/common/custom-pagination";
import { RecordScheduleFormDialog } from "@/components/admin/record-schedule-form-dialog";
import {
    useSearchAdminRecordSchedules,
    useCreateAdminRecordSchedule,
    useUpdateAdminRecordSchedule,
    useDeleteAdminRecordSchedule
} from "@/lib/api/endpoints/admin-record-schedule/admin-record-schedule";
import type {
    AdminRecordScheduleResponse,
    AdminRecordScheduleCreateRequestPlatformType,
    AdminRecordScheduleCreateRequestScheduleType,
    AdminRecordScheduleCreateRequestRecordQuality,
    AdminRecordScheduleSearchRequestPlatformType,
    AdminRecordScheduleSearchRequestScheduleType
} from "@/lib/api/models";
import { useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { PlatformBadge } from "@/components/common/platform-badge";

const DAYS_MAP: Record<string, string> = {
    MONDAY: "월",
    TUESDAY: "화",
    WEDNESDAY: "수",
    THURSDAY: "목",
    FRIDAY: "금",
    SATURDAY: "토",
    SUNDAY: "일",
};

export default function RecordSchedulesPage() {
    const queryClient = useQueryClient();

    // Dialog state
    const [isDialogOpen, setIsDialogOpen] = useState(false);
    const [dialogMode, setDialogMode] = useState<"create" | "edit">("create");
    const [selectedSchedule, setSelectedSchedule] = useState<AdminRecordScheduleResponse | null>(null);

    // Search/Filter state
    const [searchField, setSearchField] = useState<"channelName">("channelName");
    const [searchQuery, setSearchQuery] = useState("");
    const [searchPlatformType, setSearchPlatformType] = useState<string>("__none__");
    const [searchScheduleType, setSearchScheduleType] = useState<string>("__none__");

    // Pagination state
    const [page, setPage] = useState(0);
    const [size] = useState(10);

    // Build search params
    const searchParams = {
        request: {
            channelName: searchField === "channelName" ? searchQuery : undefined,
            platformType: searchPlatformType !== "__none__" ? (searchPlatformType as AdminRecordScheduleSearchRequestPlatformType) : undefined,
            scheduleType: searchScheduleType !== "__none__" ? (searchScheduleType as AdminRecordScheduleSearchRequestScheduleType) : undefined,
        },
        pageable: {
            page,
            size,
        },
    };

    // API Hooks
    const { data: schedulesData, isLoading, error } = useSearchAdminRecordSchedules(searchParams);
    const createMutation = useCreateAdminRecordSchedule();
    const updateMutation = useUpdateAdminRecordSchedule();
    const deleteMutation = useDeleteAdminRecordSchedule();

    // Handlers
    const handleSearch = () => {
        setPage(0);
    };

    const handleReset = () => {
        setSearchField("channelName");
        setSearchQuery("");
        setSearchPlatformType("__none__");
        setSearchScheduleType("__none__");
        setPage(0);
    };

    const handleOpenCreateDialog = () => {
        setDialogMode("create");
        setSelectedSchedule(null);
        setIsDialogOpen(true);
    };

    const handleOpenEditDialog = (schedule: AdminRecordScheduleResponse) => {
        setDialogMode("edit");
        setSelectedSchedule(schedule);
        setIsDialogOpen(true);
    };

    const handleDialogClose = () => {
        setIsDialogOpen(false);
        setSelectedSchedule(null);
    };

    const handleDialogSubmit = async (data: {
        channelId: number;
        platformType: AdminRecordScheduleCreateRequestPlatformType;
        scheduleType: AdminRecordScheduleCreateRequestScheduleType;
        value: string;
        recordQuality: AdminRecordScheduleCreateRequestRecordQuality;
        priority: number;
    }) => {
        try {
            if (dialogMode === "create") {
                await createMutation.mutateAsync({ data });
                toast.success("녹화 스케줄이 생성되었습니다.");
            } else if (selectedSchedule) {
                await updateMutation.mutateAsync({
                    id: selectedSchedule.id,
                    data: {
                        platformType: data.platformType,
                        scheduleType: data.scheduleType,
                        value: data.value,
                        recordQuality: data.recordQuality,
                        priority: data.priority,
                    },
                });
                toast.success("녹화 스케줄이 수정되었습니다.");
            }

            // 무효화 및 재조회
            queryClient.invalidateQueries({ queryKey: ["/admin/record-schedules"] });
            handleDialogClose();
        } catch (error) {
            toast.error(dialogMode === "create" ? "녹화 스케줄 생성에 실패했습니다." : "녹화 스케줄 수정에 실패했습니다.");
        }
    };

    const handleDelete = async (schedule: AdminRecordScheduleResponse) => {
        if (!confirm(`정말로 삭제하시겠습니까?`)) {
            return;
        }

        try {
            await deleteMutation.mutateAsync({ id: schedule.id });
            toast.success("녹화 스케줄이 삭제되었습니다.");
            queryClient.invalidateQueries({ queryKey: ["/admin/record-schedules"] });
        } catch (error) {
            toast.error("녹화 스케줄 삭제에 실패했습니다.");
        }
    };

    const formatDate = (dateString: string) => {
        return new Date(dateString).toLocaleDateString("ko-KR");
    };

    const formatScheduleValue = (type: string, value: string) => {
        if (type === "ONCE") return "한 번만";
        if (type === "ALWAYS") return "항상";
        if (type === "N_DAYS_OF_EVERY_WEEK") {
            try {
                const days = JSON.parse(value) as string[];
                return days.map(d => DAYS_MAP[d] || d).join(", ");
            } catch {
                return value;
            }
        }
        if (type === "SPECIFIC_DAY") {
            try {
                const dates = JSON.parse(value) as string[];
                return dates.join(", ");
            } catch {
                return value;
            }
        }
        return value;
    };

    const getScheduleTypeBadge = (type: string) => {
        switch (type) {
            case "ONCE": return <Badge variant="secondary">한 번만</Badge>;
            case "ALWAYS": return <Badge>항상</Badge>;
            case "N_DAYS_OF_EVERY_WEEK": return <Badge variant="outline">매주 n요일</Badge>;
            case "SPECIFIC_DAY": return <Badge variant="outline">날짜 지정</Badge>;
            default: return <Badge variant="outline">{type}</Badge>;
        }
    };

    return (
        <div className="min-w-0">
            <h2 className="text-2xl font-bold">녹화 스케줄 관리</h2>
            <p className="text-muted-foreground">채널별 녹화 스케줄을 관리합니다.</p>

            <div className="flex flex-col gap-4 mt-6 lg:flex-row lg:items-center lg:justify-between">
                {/* 왼쪽: 검색 및 필터 영역 */}
                <div className="flex flex-col gap-2 sm:flex-row sm:flex-wrap">
                    <Select value={searchField} onValueChange={(value) => setSearchField(value as "channelName")}>
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
                    <Select value={searchPlatformType} onValueChange={setSearchPlatformType}>
                        <SelectTrigger className="w-full sm:w-auto sm:min-w-[120px]">
                            <span className="text-muted-foreground">플랫폼:</span>
                            <SelectValue placeholder="플랫폼" />
                        </SelectTrigger>
                        <SelectContent>
                            <SelectGroup>
                                <SelectItem value="__none__">전체</SelectItem>
                                <SelectItem value="CHZZK">치지직</SelectItem>
                                <SelectItem value="TWITCH">트위치</SelectItem>
                                <SelectItem value="SOOP">SOOP</SelectItem>
                            </SelectGroup>
                        </SelectContent>
                    </Select>
                    <Select value={searchScheduleType} onValueChange={setSearchScheduleType}>
                        <SelectTrigger className="w-full sm:w-auto sm:min-w-[120px]">
                            <span className="text-muted-foreground">유형:</span>
                            <SelectValue placeholder="스케줄 유형" />
                        </SelectTrigger>
                        <SelectContent>
                            <SelectGroup>
                                <SelectItem value="__none__">전체</SelectItem>
                                <SelectItem value="ONCE">한 번만</SelectItem>
                                <SelectItem value="ALWAYS">항상</SelectItem>
                                <SelectItem value="N_DAYS_OF_EVERY_WEEK">매주 n요일</SelectItem>
                                <SelectItem value="SPECIFIC_DAY">날짜 지정</SelectItem>
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

                {/* 오른쪽: 액션 버튼 */}
                <Button className="w-full lg:w-auto" onClick={handleOpenCreateDialog}>
                    <Plus className="mr-2 h-4 w-4" />
                    스케줄 추가
                </Button>
            </div>

            {/* 스케줄 목록 */}
            <div className="w-full rounded-lg border mt-4 overflow-x-auto">
                <Table className="w-full">
                    <TableHeader className="bg-muted">
                        <TableRow>
                            <TableHead className="border-r font-semibold w-[60px] text-center">ID</TableHead>
                            <TableHead className="border-r font-semibold">채널 정보</TableHead>
                            <TableHead className="border-r font-semibold w-[100px] text-center">플랫폼</TableHead>
                            <TableHead className="border-r font-semibold w-[120px] text-center">유형</TableHead>
                            <TableHead className="border-r font-semibold">값</TableHead>
                            <TableHead className="border-r font-semibold w-[100px] text-center">화질</TableHead>
                            <TableHead className="border-r font-semibold w-[80px] text-center">우선순위</TableHead>
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
                        ) : !schedulesData?.content || schedulesData.content.length === 0 ? (
                            <TableRow>
                                <TableCell colSpan={9} className="text-center py-8 text-muted-foreground">
                                    등록된 녹화 스케줄이 없습니다.
                                </TableCell>
                            </TableRow>
                        ) : (
                            schedulesData.content.map((schedule) => (
                                <TableRow key={schedule.id}>
                                    {/* ID */}
                                    <TableCell className="border-r text-center">{schedule.id}</TableCell>

                                    {/* 채널 정보 */}
                                    <TableCell className="border-r">
                                        <div className="flex items-center gap-2">
                                            <Avatar className="h-8 w-8">
                                                <AvatarImage src={schedule.channel.profileUrl} />
                                                <AvatarFallback>{schedule.channel.name[0]?.toUpperCase()}</AvatarFallback>
                                            </Avatar>
                                            <span>{schedule.channel.name}</span>
                                        </div>
                                    </TableCell>

                                    {/* 플랫폼 */}
                                    <TableCell className="border-r text-center">
                                        <PlatformBadge platform={schedule.platformType} />
                                    </TableCell>

                                    {/* 유형 */}
                                    <TableCell className="border-r text-center">
                                        {getScheduleTypeBadge(schedule.scheduleType)}
                                    </TableCell>

                                    {/* 값 */}
                                    <TableCell className="border-r">
                                        <span className="text-sm">
                                            {formatScheduleValue(schedule.scheduleType, schedule.value)}
                                        </span>
                                    </TableCell>

                                    {/* 화질 */}
                                    <TableCell className="border-r text-center">
                                        <Badge variant={schedule.recordQuality === "BEST" ? "default" : "secondary"}>
                                            {schedule.recordQuality === "BEST" ? "최고 화질" : "최저 화질"}
                                        </Badge>
                                    </TableCell>

                                    {/* 우선순위 */}
                                    <TableCell className="border-r text-center">{schedule.priority}</TableCell>

                                    {/* 생성일 */}
                                    <TableCell className="border-r text-center">{formatDate(schedule.createdAt)}</TableCell>

                                    {/* 작업 */}
                                    <TableCell className="text-center">
                                        <div className="flex gap-2 justify-center">
                                            <Button
                                                variant="secondary"
                                                size="icon"
                                                onClick={() => handleOpenEditDialog(schedule)}
                                                disabled={updateMutation.isPending}
                                            >
                                                <Edit className="h-4 w-4" />
                                            </Button>
                                            <Button
                                                variant="secondary"
                                                size="icon"
                                                onClick={() => handleDelete(schedule)}
                                                disabled={deleteMutation.isPending}
                                            >
                                                <Trash2 className="h-4 w-4" />
                                            </Button>
                                        </div>
                                    </TableCell>
                                </TableRow>
                            ))
                        )}
                    </TableBody>
                </Table>
            </div>

            {/* 페이지네이션 */}
            {schedulesData && (
                <CustomPagination
                    page={page}
                    totalPages={schedulesData.totalPages || 0}
                    onPageChange={setPage}
                />
            )}

            {/* 다이얼로그 */}
            <RecordScheduleFormDialog
                open={isDialogOpen}
                onOpenChange={handleDialogClose}
                mode={dialogMode}
                schedule={selectedSchedule}
                onSubmit={handleDialogSubmit}
                isSubmitting={createMutation.isPending || updateMutation.isPending}
            />
        </div>
    );
}