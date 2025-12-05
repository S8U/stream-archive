"use client";

import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Button } from "@/components/ui/button";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Edit, Loader2, Plus, Trash2, ExternalLink } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { useState } from "react";
import { useSearchParams } from "next/navigation";
import { CustomPagination } from "@/components/common/custom-pagination";
import { ChannelPlatformFormDialog } from "@/components/admin/channel-platform-form-dialog";
import {
    useSearchAdminChannelPlatforms,
    useCreateAdminChannelPlatform,
    useUpdateAdminChannelPlatform,
    useDeleteAdminChannelPlatform
} from "@/lib/api/endpoints/admin-channel-platform/admin-channel-platform";
import type {
    AdminChannelPlatformResponse,
    AdminChannelPlatformCreateRequestPlatformType,
    AdminChannelPlatformSearchRequestPlatformType
} from "@/lib/api/models";
import { useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import Link from "next/link";
import { PlatformBadge } from "@/components/common/platform-badge";

export default function ChannelPlatformsPage() {
    const queryClient = useQueryClient();
    const urlSearchParams = useSearchParams();

    // Dialog state
    const [isDialogOpen, setIsDialogOpen] = useState(false);
    const [dialogMode, setDialogMode] = useState<"create" | "edit">("create");
    const [selectedPlatform, setSelectedPlatform] = useState<AdminChannelPlatformResponse | null>(null);

    // Search/Filter state
    const [searchField, setSearchField] = useState<"channelName" | "platformChannelId">("channelName");
    const [searchQuery, setSearchQuery] = useState("");
    const [searchPlatformType, setSearchPlatformType] = useState<string>("__none__");
    const [searchIsSyncProfile, setSearchIsSyncProfile] = useState<string>("__none__");

    // Pagination state - URL에서 초기값 읽기
    const initialPage = Math.max(0, Number(urlSearchParams.get("page") || 1) - 1);
    const [page, setPage] = useState(initialPage);
    const [size] = useState(10);

    // Build search params
    const searchParams = {
        request: {
            channelName: searchField === "channelName" ? searchQuery : undefined,
            platformChannelId: searchField === "platformChannelId" ? searchQuery : undefined,
            platformType: searchPlatformType !== "__none__" ? (searchPlatformType as AdminChannelPlatformSearchRequestPlatformType) : undefined,
            isSyncProfile: searchIsSyncProfile !== "__none__" ? searchIsSyncProfile === "true" : undefined,
        },
        pageable: {
            page,
            size,
        },
    };

    // API Hooks
    const { data: platformsData, isLoading, error } = useSearchAdminChannelPlatforms(searchParams);
    const createMutation = useCreateAdminChannelPlatform();
    const updateMutation = useUpdateAdminChannelPlatform();
    const deleteMutation = useDeleteAdminChannelPlatform();

    // Handlers
    const handleSearch = () => {
        setPage(0);
    };

    const handleReset = () => {
        setSearchField("channelName");
        setSearchQuery("");
        setSearchPlatformType("__none__");
        setSearchIsSyncProfile("__none__");
        setPage(0);
    };

    const handleOpenCreateDialog = () => {
        setDialogMode("create");
        setSelectedPlatform(null);
        setIsDialogOpen(true);
    };

    const handleOpenEditDialog = (platform: AdminChannelPlatformResponse) => {
        setDialogMode("edit");
        setSelectedPlatform(platform);
        setIsDialogOpen(true);
    };

    const handleDialogClose = () => {
        setIsDialogOpen(false);
        setSelectedPlatform(null);
    };

    const handleDialogSubmit = async (data: {
        channelId: number;
        platformType: AdminChannelPlatformCreateRequestPlatformType;
        platformChannelId: string;
        isSyncProfile: boolean;
    }) => {
        try {
            if (dialogMode === "create") {
                await createMutation.mutateAsync({ data });
                toast.success("플랫폼 연결이 생성되었습니다.");
            } else if (selectedPlatform) {
                await updateMutation.mutateAsync({
                    id: selectedPlatform.id,
                    data: {
                        isSyncProfile: data.isSyncProfile,
                        platformChannelId: data.platformChannelId
                    },
                });
                toast.success("플랫폼 연결이 수정되었습니다.");
            }

            // 무효화 및 재조회
            queryClient.invalidateQueries({ queryKey: ["/admin/channel-platforms"] });
            handleDialogClose();
        } catch (error) {
            toast.error(dialogMode === "create" ? "플랫폼 연결 생성에 실패했습니다." : "플랫폼 연결 수정에 실패했습니다.");
        }
    };

    const handleDelete = async (platform: AdminChannelPlatformResponse) => {
        if (!confirm(`정말로 삭제하시겠습니까?`)) {
            return;
        }

        try {
            await deleteMutation.mutateAsync({ id: platform.id });
            toast.success("플랫폼 연결이 삭제되었습니다.");
            queryClient.invalidateQueries({ queryKey: ["/admin/channel-platforms"] });
        } catch (error) {
            toast.error("플랫폼 연결 삭제에 실패했습니다.");
        }
    };

    const formatDate = (dateString: string) => {
        return new Date(dateString).toLocaleDateString("ko-KR");
    };

    return (
        <div className="min-w-0">
            <h2 className="text-2xl font-bold">채널 플랫폼 관리</h2>
            <p className="text-muted-foreground">채널별 플랫폼 연결 정보를 관리합니다.</p>

            <div className="flex flex-col gap-4 mt-6 lg:flex-row lg:items-center lg:justify-between">
                {/* 왼쪽: 검색 및 필터 영역 */}
                <div className="flex flex-col gap-2 sm:flex-row sm:flex-wrap">
                    <Select value={searchField} onValueChange={(value) => setSearchField(value as "channelName" | "platformChannelId")}>
                        <SelectTrigger className="w-full sm:w-auto sm:min-w-[120px]">
                            <span className="text-muted-foreground">검색 기준:</span>
                            <SelectValue placeholder="검색 기준" />
                        </SelectTrigger>
                        <SelectContent>
                            <SelectGroup>
                                <SelectItem value="channelName">채널명</SelectItem>
                                <SelectItem value="platformChannelId">플랫폼 채널 ID</SelectItem>
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
                    <Select value={searchIsSyncProfile} onValueChange={setSearchIsSyncProfile}>
                        <SelectTrigger className="w-full sm:w-auto sm:min-w-[120px]">
                            <span className="text-muted-foreground">동기화:</span>
                            <SelectValue placeholder="동기화 여부" />
                        </SelectTrigger>
                        <SelectContent>
                            <SelectGroup>
                                <SelectItem value="__none__">전체</SelectItem>
                                <SelectItem value="true">켜짐</SelectItem>
                                <SelectItem value="false">꺼짐</SelectItem>
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
                    플랫폼 연결 추가
                </Button>
            </div>

            {/* 플랫폼 목록 */}
            <div className="w-full rounded-lg border mt-4 overflow-x-auto">
                <Table className="w-full">
                    <TableHeader className="bg-muted">
                        <TableRow>
                            <TableHead className="border-r font-semibold w-[60px] text-center">ID</TableHead>
                            <TableHead className="border-r font-semibold w-[80px] text-center">채널 ID</TableHead>
                            <TableHead className="border-r font-semibold">채널 정보</TableHead>
                            <TableHead className="border-r font-semibold w-[100px] text-center">플랫폼</TableHead>
                            <TableHead className="border-r font-semibold">플랫폼 채널 ID</TableHead>
                            <TableHead className="border-r font-semibold w-[100px] text-center">프로필 동기화</TableHead>
                            <TableHead className="border-r font-semibold w-[120px] text-center">생성일</TableHead>
                            <TableHead className="font-semibold w-[100px] text-center">작업</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {isLoading ? (
                            <TableRow>
                                <TableCell colSpan={8} className="text-center py-8">
                                    <Loader2 className="h-6 w-6 animate-spin mx-auto" />
                                </TableCell>
                            </TableRow>
                        ) : error ? (
                            <TableRow>
                                <TableCell colSpan={8} className="text-center py-8 text-destructive">
                                    데이터를 불러오는 중 오류가 발생했습니다.
                                </TableCell>
                            </TableRow>
                        ) : !platformsData?.content || platformsData.content.length === 0 ? (
                            <TableRow>
                                <TableCell colSpan={8} className="text-center py-8 text-muted-foreground">
                                    등록된 플랫폼 연결이 없습니다.
                                </TableCell>
                            </TableRow>
                        ) : (
                            platformsData.content.map((platform) => (
                                <TableRow key={platform.id}>
                                    {/* ID */}
                                    <TableCell className="border-r text-center">{platform.id}</TableCell>

                                    {/* 채널 ID */}
                                    <TableCell className="border-r text-center">{platform.channel.id}</TableCell>

                                    {/* 채널 정보 */}
                                    <TableCell className="border-r">
                                        <div className="flex items-center gap-2">
                                            <Avatar className="h-8 w-8">
                                                <AvatarImage src={platform.channel.profileUrl} />
                                                <AvatarFallback>{platform.channel.name[0]?.toUpperCase()}</AvatarFallback>
                                            </Avatar>
                                            <span>{platform.channel.name}</span>
                                        </div>
                                    </TableCell>

                                    {/* 플랫폼 */}
                                    <TableCell className="border-r text-center">
                                        <PlatformBadge platform={platform.platformType} />
                                    </TableCell>

                                    {/* 플랫폼 채널 ID */}
                                    <TableCell className="border-r">
                                        <Link
                                            href={platform.platformUrl}
                                            target="_blank"
                                            rel="noopener noreferrer"
                                            className="flex items-center gap-2 hover:underline"
                                        >
                                            {platform.platformChannelId}
                                            <ExternalLink className="h-3 w-3" />
                                        </Link>
                                    </TableCell>

                                    {/* 프로필 동기화 */}
                                    <TableCell className="border-r text-center">
                                        {platform.isSyncProfile ? (
                                            <Badge className="bg-emerald-100 text-emerald-700 hover:bg-emerald-100/80">켜짐</Badge>
                                        ) : (
                                            <Badge variant="secondary">꺼짐</Badge>
                                        )}
                                    </TableCell>

                                    {/* 생성일 */}
                                    <TableCell className="border-r text-center">{formatDate(platform.createdAt)}</TableCell>

                                    {/* 작업 */}
                                    <TableCell className="text-center">
                                        <div className="flex gap-2 justify-center">
                                            <Button
                                                variant="secondary"
                                                size="icon"
                                                onClick={() => handleOpenEditDialog(platform)}
                                                disabled={updateMutation.isPending}
                                            >
                                                <Edit className="h-4 w-4" />
                                            </Button>
                                            <Button
                                                variant="secondary"
                                                size="icon"
                                                onClick={() => handleDelete(platform)}
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
            {platformsData && (
                <CustomPagination
                    page={page}
                    totalPages={platformsData.totalPages || 0}
                    onPageChange={setPage}
                />
            )}

            {/* 다이얼로그 */}
            <ChannelPlatformFormDialog
                open={isDialogOpen}
                onOpenChange={handleDialogClose}
                mode={dialogMode}
                platform={selectedPlatform}
                onSubmit={handleDialogSubmit}
                isSubmitting={createMutation.isPending || updateMutation.isPending}
            />
        </div>
    );
}