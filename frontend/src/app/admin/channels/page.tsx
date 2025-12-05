"use client";

import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Button } from "@/components/ui/button";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Edit, Loader2, Plus, Trash2 } from "lucide-react";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { useState } from "react";
import { useQueryState, parseAsInteger, parseAsStringLiteral } from "nuqs";
import { CustomPagination } from "@/components/common/custom-pagination";
import { ChannelFormDialog } from "@/components/admin/channel-form-dialog";
import { useSearchAdminChannels, useCreateAdminChannel, useUpdateAdminChannel, useDeleteAdminChannel } from "@/lib/api/endpoints/admin-channel/admin-channel";
import type { AdminChannelResponse, AdminChannelCreateRequestContentPrivacy, AdminChannelSearchRequestContentPrivacy } from "@/lib/api/models";
import { useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import Link from "next/link";

type SearchField = "id" | "uuid" | "name";
const searchFieldOptions = ["id", "uuid", "name"] as const;
const privacyOptions = ["__none__", "PUBLIC", "UNLISTED", "PRIVATE"] as const;

export default function ChannelsPage() {
    const queryClient = useQueryClient();

    // Dialog state
    const [isDialogOpen, setIsDialogOpen] = useState(false);
    const [dialogMode, setDialogMode] = useState<"create" | "edit">("create");
    const [selectedChannel, setSelectedChannel] = useState<AdminChannelResponse | null>(null);

    // URL 상태 (nuqs)
    const [searchField, setSearchField] = useQueryState("field", parseAsStringLiteral(searchFieldOptions).withDefault("name"));
    const [searchQuery, setSearchQuery] = useQueryState("q", { defaultValue: "" });
    const [searchContentPrivacy, setSearchContentPrivacy] = useQueryState("privacy", parseAsStringLiteral(privacyOptions).withDefault("__none__"));
    const [page, setPage] = useQueryState("page", parseAsInteger.withDefault(1));

    const size = 10;

    // Build search params
    const searchParams = {
        request: {
            id: searchField === "id" && searchQuery ? Number(searchQuery) : undefined,
            uuid: searchField === "uuid" ? searchQuery : undefined,
            name: searchField === "name" ? searchQuery : undefined,
            contentPrivacy: searchContentPrivacy !== "__none__" ? (searchContentPrivacy as AdminChannelSearchRequestContentPrivacy) : undefined,
        },
        pageable: {
            page: page - 1,
            size,
        },
    };

    // API Hooks
    const { data: channelsData, isLoading, error } = useSearchAdminChannels(searchParams);
    const createMutation = useCreateAdminChannel();
    const updateMutation = useUpdateAdminChannel();
    const deleteMutation = useDeleteAdminChannel();

    // Handlers
    const handleSearch = () => {
        setPage(1);
    };

    const handleReset = () => {
        setSearchField("name");
        setSearchQuery("");
        setSearchContentPrivacy("__none__");
        setPage(1);
    };

    const handleOpenCreateDialog = () => {
        setDialogMode("create");
        setSelectedChannel(null);
        setIsDialogOpen(true);
    };

    const handleOpenEditDialog = (channel: AdminChannelResponse) => {
        setDialogMode("edit");
        setSelectedChannel(channel);
        setIsDialogOpen(true);
    };

    const handleDialogClose = () => {
        setIsDialogOpen(false);
        setSelectedChannel(null);
    };

    const handleDialogSubmit = async (data: { name: string; contentPrivacy: AdminChannelCreateRequestContentPrivacy }) => {
        try {
            if (dialogMode === "create") {
                await createMutation.mutateAsync({ data });
                toast.success(`"${data.name}" 채널이 생성되었습니다.`);
            } else if (selectedChannel) {
                await updateMutation.mutateAsync({
                    id: selectedChannel.id,
                    data,
                });
                toast.success(`"${data.name}" 채널이 수정되었습니다.`);
            }

            // Invalidate and refetch
            queryClient.invalidateQueries({ queryKey: ["/admin/channels"] });
            handleDialogClose();
        } catch (error) {
            toast.error(dialogMode === "create" ? "채널 생성에 실패했습니다." : "채널 수정에 실패했습니다.");
        }
    };

    const handleDelete = async (channel: AdminChannelResponse) => {
        if (!confirm(`"${channel.name}" 채널을 삭제하시겠습니까?`)) {
            return;
        }

        try {
            await deleteMutation.mutateAsync({ id: channel.id });
            toast.success(`"${channel.name}" 채널이 삭제되었습니다.`);
            queryClient.invalidateQueries({ queryKey: ["/admin/channels"] });
        } catch (error) {
            toast.error("채널 삭제에 실패했습니다.");
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

    return (
        <div className="min-w-0">
            <h2 className="text-2xl font-bold">채널 관리</h2>
            <p className="text-muted-foreground">등록된 채널 목록을 관리합니다.</p>

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
                                <SelectItem value="id">ID</SelectItem>
                                <SelectItem value="uuid">UUID</SelectItem>
                                <SelectItem value="name">채널명</SelectItem>
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

                {/* 오른쪽: 액션 버튼 */}
                <Button className="w-full lg:w-auto" onClick={handleOpenCreateDialog}>
                    <Plus />
                    채널 추가
                </Button>
            </div>

            {/* 채널 목록 */}
            <div className="w-full rounded-lg border mt-4 overflow-x-auto">
                <Table className="w-full">
                    <TableHeader className="bg-muted">
                        <TableRow>
                            <TableHead className="border-r font-semibold w-[60px] text-center">ID</TableHead>
                            <TableHead className="border-r font-semibold">채널 정보</TableHead>
                            <TableHead className="border-r font-semibold w-[350px]">UUID</TableHead>
                            <TableHead className="border-r font-semibold w-[100px] text-center">공개 범위</TableHead>
                            <TableHead className="border-r font-semibold w-[120px] text-center">생성일</TableHead>
                            <TableHead className="font-semibold w-[100px] text-center">작업</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {isLoading ? (
                            <TableRow>
                                <TableCell colSpan={6} className="text-center py-8">
                                    <Loader2 className="h-6 w-6 animate-spin mx-auto" />
                                </TableCell>
                            </TableRow>
                        ) : error ? (
                            <TableRow>
                                <TableCell colSpan={6} className="text-center py-8 text-destructive">
                                    데이터를 불러오는 중 오류가 발생했습니다.
                                </TableCell>
                            </TableRow>
                        ) : !channelsData?.content || channelsData.content.length === 0 ? (
                            <TableRow>
                                <TableCell colSpan={6} className="text-center py-8 text-muted-foreground">
                                    등록된 채널이 없습니다.
                                </TableCell>
                            </TableRow>
                        ) : (
                            channelsData.content.map((channel) => (
                                <TableRow key={channel.id}>
                                    {/* ID */}
                                    <TableCell className="border-r text-center">{channel.id}</TableCell>

                                    {/* 채널 정보 (프로필 + 이름) */}
                                    <TableCell className="border-r">
                                        <Link
                                            href={`/channels/${channel.uuid}`}
                                            target="_blank"
                                            rel="noopener noreferrer"
                                            className="flex items-center gap-2 hover:underline"
                                        >
                                            <Avatar>
                                                <AvatarImage src={channel.profileUrl} />
                                                <AvatarFallback>{channel.name[0]?.toUpperCase()}</AvatarFallback>
                                            </Avatar>
                                            <span>{channel.name}</span>
                                        </Link>
                                    </TableCell>

                                    {/* UUID */}
                                    <TableCell className="border-r font-mono text-xs">{channel.uuid}</TableCell>

                                    {/* 공개 범위 */}
                                    <TableCell className="border-r text-center">
                                        <Badge variant={getPrivacyVariant(channel.contentPrivacy)}>
                                            {getPrivacyLabel(channel.contentPrivacy)}
                                        </Badge>
                                    </TableCell>

                                    {/* 생성일 */}
                                    <TableCell className="border-r text-center">{formatDate(channel.createdAt)}</TableCell>

                                    {/* 작업 (수정/삭제) */}
                                    <TableCell className="text-center">
                                        <div className="flex gap-2 justify-center">
                                            <Button
                                                variant="secondary"
                                                size="icon"
                                                onClick={() => handleOpenEditDialog(channel)}
                                                disabled={updateMutation.isPending}
                                            >
                                                <Edit />
                                            </Button>
                                            <Button
                                                variant="secondary"
                                                size="icon"
                                                onClick={() => handleDelete(channel)}
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
            {channelsData && (
                <CustomPagination
                    page={page - 1}
                    totalPages={channelsData.totalPages || 0}
                    onPageChange={(p) => setPage(p + 1)}
                />
            )}

            {/* 채널 생성/수정 다이얼로그 */}
            <ChannelFormDialog
                open={isDialogOpen}
                onOpenChange={handleDialogClose}
                mode={dialogMode}
                channel={selectedChannel}
                onSubmit={handleDialogSubmit}
                isSubmitting={createMutation.isPending || updateMutation.isPending}
            />
        </div>
    );
}