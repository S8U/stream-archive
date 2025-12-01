"use client";

import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Button } from "@/components/ui/button";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Edit, Loader2, Plus, Trash2 } from "lucide-react";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import {
    Dialog,
    DialogClose,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from "@/components/ui/dialog";
import { Label } from "@/components/ui/label";
import { useState } from "react";
import { CustomPagination } from "@/components/common/custom-pagination";
import { useSearchAdminChannels, useCreateAdminChannel, useUpdateAdminChannel, useDeleteAdminChannel } from "@/lib/api/endpoints/admin-channel/admin-channel";
import type { AdminChannelResponse, AdminChannelCreateRequestContentPrivacy, AdminChannelSearchRequestContentPrivacy } from "@/lib/api/models";
import { useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";

type SearchField = "__none__" | "id" | "uuid" | "name";

export default function ChannelsPage() {
    const queryClient = useQueryClient();

    // Modal state
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [modalMode, setModalMode] = useState<"create" | "edit">("create");
    const [selectedChannel, setSelectedChannel] = useState<AdminChannelResponse | null>(null);

    // Search/Filter state
    const [searchField, setSearchField] = useState<SearchField>("__none__");
    const [searchQuery, setSearchQuery] = useState("");
    const [contentPrivacy, setContentPrivacy] = useState<string>("__none__");

    // Pagination state
    const [page, setPage] = useState(0);
    const [size] = useState(10);

    // Form state
    const [formName, setFormName] = useState("");
    const [formPrivacy, setFormPrivacy] = useState<AdminChannelCreateRequestContentPrivacy>("PUBLIC");

    // Build search params
    const searchParams = {
        request: {
            name: searchField === "name" ? searchQuery : undefined,
            contentPrivacy: contentPrivacy !== "__none__" ? (contentPrivacy as AdminChannelSearchRequestContentPrivacy) : undefined,
        },
        pageable: {
            page,
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
        setPage(0);
    };

    const handleReset = () => {
        setSearchField("__none__");
        setSearchQuery("");
        setContentPrivacy("__none__");
        setPage(0);
    };

    const handleOpenCreateModal = () => {
        setModalMode("create");
        setSelectedChannel(null);
        setFormName("");
        setFormPrivacy("PUBLIC");
        setIsModalOpen(true);
    };

    const handleOpenEditModal = (channel: AdminChannelResponse) => {
        setModalMode("edit");
        setSelectedChannel(channel);
        setFormName(channel.name);
        setFormPrivacy(channel.contentPrivacy as AdminChannelCreateRequestContentPrivacy);
        setIsModalOpen(true);
    };

    const handleModalClose = () => {
        setIsModalOpen(false);
        setSelectedChannel(null);
        setFormName("");
        setFormPrivacy("PUBLIC");
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        try {
            if (modalMode === "create") {
                await createMutation.mutateAsync({
                    data: {
                        name: formName,
                        contentPrivacy: formPrivacy,
                    },
                });
                toast.success(`"${formName}" 채널이 생성되었습니다.`);
            } else if (selectedChannel) {
                await updateMutation.mutateAsync({
                    id: selectedChannel.id,
                    data: {
                        name: formName,
                        contentPrivacy: formPrivacy,
                    },
                });
                toast.success(`"${formName}" 채널이 수정되었습니다.`);
            }

            // Invalidate and refetch
            queryClient.invalidateQueries({ queryKey: ["/admin/channels"] });
            handleModalClose();
        } catch (error) {
            toast.error(modalMode === "create" ? "채널 생성에 실패했습니다." : "채널 수정에 실패했습니다.");
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

    return (
        <div className="min-w-0">
            <h2 className="text-2xl font-bold">채널 관리</h2>
            <p className="text-muted-foreground">등록된 채널 목록을 관리합니다.</p>

            <div className="flex flex-col gap-4 mt-6 lg:flex-row lg:items-center lg:justify-between">
                {/* 왼쪽: 검색 및 필터 영역 */}
                <div className="flex flex-col gap-2 sm:flex-row sm:flex-wrap">
                    <Select value={searchField} onValueChange={(value) => setSearchField(value as SearchField)}>
                        <SelectTrigger className="w-full sm:w-auto sm:min-w-[120px]">
                            <SelectValue placeholder="검색 기준" />
                        </SelectTrigger>
                        <SelectContent>
                            <SelectGroup>
                                <SelectItem value="__none__">전체</SelectItem>
                                <SelectItem value="name">채널명</SelectItem>
                            </SelectGroup>
                        </SelectContent>
                    </Select>
                    <Select value={contentPrivacy} onValueChange={setContentPrivacy}>
                        <SelectTrigger className="w-full sm:w-auto sm:min-w-[128px]">
                            <SelectValue placeholder="공개범위" />
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
                        placeholder="검색"
                        className="w-full sm:flex-1 sm:min-w-[200px]"
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                        disabled={searchField === "__none__"}
                    />
                    <Button variant="default" onClick={handleSearch}>검색</Button>
                    <Button variant="outline" onClick={handleReset}>초기화</Button>
                </div>

                {/* 오른쪽: 액션 버튼 */}
                <Button className="w-full lg:w-auto" onClick={handleOpenCreateModal}>
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
                            <TableHead className="border-r font-semibold w-[100px] text-center">공개범위</TableHead>
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
                                    <TableCell className="border-r text-center">{channel.id}</TableCell>
                                    <TableCell className="border-r">
                                        <div className="flex items-center gap-2">
                                            <Avatar>
                                                <AvatarImage src={channel.profileUrl} />
                                                <AvatarFallback>{channel.name[0]?.toUpperCase()}</AvatarFallback>
                                            </Avatar>
                                            <span>{channel.name}</span>
                                        </div>
                                    </TableCell>
                                    <TableCell className="border-r font-mono text-xs">{channel.uuid}</TableCell>
                                    <TableCell className="border-r text-center">{getPrivacyLabel(channel.contentPrivacy)}</TableCell>
                                    <TableCell className="border-r text-center">{formatDate(channel.createdAt)}</TableCell>
                                    <TableCell className="text-center">
                                        <div className="flex gap-2 justify-center">
                                            <Button
                                                variant="secondary"
                                                size="icon"
                                                onClick={() => handleOpenEditModal(channel)}
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
                    page={page}
                    totalPages={channelsData.totalPages || 0}
                    onPageChange={setPage}
                />
            )}

            {/* 채널 생성/수정 모달 */}
            <Dialog open={isModalOpen} onOpenChange={handleModalClose}>
                <DialogContent className="sm:max-w-100">
                    <form onSubmit={handleSubmit} className="flex flex-col gap-4">
                        <DialogHeader>
                            <DialogTitle>{modalMode === "create" ? "채널 생성" : "채널 수정"}</DialogTitle>
                            <DialogDescription>
                                {modalMode === "create" ? "새로운 채널을 생성합니다." : "채널 정보를 수정합니다."}
                            </DialogDescription>
                        </DialogHeader>
                        <div className="grid gap-4">
                            <div className="grid gap-3">
                                <Label>이름</Label>
                                <Input
                                    type="text"
                                    required
                                    value={formName}
                                    onChange={(e) => setFormName(e.target.value)}
                                />
                            </div>
                            <div className="grid gap-3">
                                <Label>공개 범위</Label>
                                <Select value={formPrivacy} onValueChange={(value) => setFormPrivacy(value as AdminChannelCreateRequestContentPrivacy)}>
                                    <SelectTrigger className="w-full">
                                        <SelectValue placeholder="선택" />
                                    </SelectTrigger>
                                    <SelectContent>
                                        <SelectGroup>
                                            <SelectItem value="PUBLIC">공개</SelectItem>
                                            <SelectItem value="UNLISTED">일부 공개</SelectItem>
                                            <SelectItem value="PRIVATE">비공개</SelectItem>
                                        </SelectGroup>
                                    </SelectContent>
                                </Select>
                            </div>
                        </div>
                        <DialogFooter>
                            <DialogClose asChild>
                                <Button variant="outline" type="button">취소</Button>
                            </DialogClose>
                            <Button type="submit" disabled={createMutation.isPending || updateMutation.isPending}>
                                {createMutation.isPending || updateMutation.isPending ? (
                                    <>
                                        <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                                        처리중...
                                    </>
                                ) : modalMode === "create" ? (
                                    "생성"
                                ) : (
                                    "수정"
                                )}
                            </Button>
                        </DialogFooter>
                    </form>
                </DialogContent>
            </Dialog>
        </div>
    );
}