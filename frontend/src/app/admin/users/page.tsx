"use client";

import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Button } from "@/components/ui/button";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Edit, Loader2, Trash2 } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { useState } from "react";
import { CustomPagination } from "@/components/common/custom-pagination";
import { useSearchAdminUsers, useUpdateAdminUser, useDeleteAdminUser } from "@/lib/api/endpoints/admin-user/admin-user";
import type { AdminUserResponse, AdminUserSearchRequestRole, AdminUserUpdateRequestRole } from "@/lib/api/models";
import { useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { UserFormDialog } from "@/components/admin/user-form-dialog";

type SearchField = "username" | "name" | "email";

export default function UsersPage() {
    const queryClient = useQueryClient();

    // Dialog state
    const [isDialogOpen, setIsDialogOpen] = useState(false);
    const [selectedUser, setSelectedUser] = useState<AdminUserResponse | null>(null);

    // Search/Filter state
    const [searchField, setSearchField] = useState<SearchField>("username");
    const [searchQuery, setSearchQuery] = useState("");
    const [searchRole, setSearchRole] = useState<string>("__none__");

    // Pagination state
    const [page, setPage] = useState(0);
    const [size] = useState(10);

    // Build search params
    const searchParams = {
        request: {
            keyword: searchQuery || undefined,
            role: searchRole !== "__none__" ? (searchRole as AdminUserSearchRequestRole) : undefined,
        },
        pageable: {
            page,
            size,
        },
    };

    // API Hooks
    const { data: usersData, isLoading, error } = useSearchAdminUsers(searchParams);
    const updateMutation = useUpdateAdminUser();
    const deleteMutation = useDeleteAdminUser();

    // Handlers
    const handleSearch = () => {
        setPage(0);
    };

    const handleReset = () => {
        setSearchField("username");
        setSearchQuery("");
        setSearchRole("__none__");
        setPage(0);
    };

    const handleOpenEditDialog = (user: AdminUserResponse) => {
        setSelectedUser(user);
        setIsDialogOpen(true);
    };

    const handleDialogClose = () => {
        setIsDialogOpen(false);
        setSelectedUser(null);
    };

    const handleDialogSubmit = async (data: { role: AdminUserUpdateRequestRole }) => {
        try {
            if (selectedUser) {
                await updateMutation.mutateAsync({
                    id: selectedUser.id,
                    data: { role: data.role },
                });
                toast.success(`"${selectedUser.username}" 사용자의 역할이 변경되었습니다.`);
            }

            // Invalidate and refetch
            queryClient.invalidateQueries({ queryKey: ["/admin/users"] });
            handleDialogClose();
        } catch (error) {
            toast.error("사용자 수정에 실패했습니다.");
        }
    };

    const handleDelete = async (user: AdminUserResponse) => {
        if (!confirm(`"${user.username}" 사용자를 삭제하시겠습니까?\n이 작업은 되돌릴 수 없습니다.`)) {
            return;
        }

        try {
            await deleteMutation.mutateAsync({ id: user.id });
            toast.success(`"${user.username}" 사용자가 삭제되었습니다.`);
            queryClient.invalidateQueries({ queryKey: ["/admin/users"] });
        } catch (error) {
            toast.error("사용자 삭제에 실패했습니다.");
        }
    };

    const formatDate = (dateString: string) => {
        return new Date(dateString).toLocaleDateString("ko-KR");
    };

    const formatDateTime = (dateString: string | undefined) => {
        if (!dateString) return "-";
        return new Date(dateString).toLocaleString("ko-KR");
    };

    const getRoleLabel = (role: string) => {
        switch (role) {
            case "ADMIN":
                return "관리자";
            case "USER":
                return "사용자";
            default:
                return role;
        }
    };

    const getRoleVariant = (role: string): "default" | "secondary" | "outline" | "destructive" => {
        switch (role) {
            case "ADMIN":
                return "destructive";
            case "USER":
                return "secondary";
            default:
                return "default";
        }
    };

    return (
        <div className="min-w-0">
            <h2 className="text-2xl font-bold">사용자 관리</h2>
            <p className="text-muted-foreground">등록된 사용자 목록을 관리합니다.</p>

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
                                <SelectItem value="username">아이디</SelectItem>
                                <SelectItem value="name">이름</SelectItem>
                                <SelectItem value="email">이메일</SelectItem>
                            </SelectGroup>
                        </SelectContent>
                    </Select>
                    <Select value={searchRole} onValueChange={setSearchRole}>
                        <SelectTrigger className="w-full sm:w-auto sm:min-w-[120px]">
                            <span className="text-muted-foreground">역할:</span>
                            <SelectValue placeholder="역할" />
                        </SelectTrigger>
                        <SelectContent>
                            <SelectGroup>
                                <SelectItem value="__none__">전체</SelectItem>
                                <SelectItem value="ADMIN">관리자</SelectItem>
                                <SelectItem value="USER">사용자</SelectItem>
                            </SelectGroup>
                        </SelectContent>
                    </Select>
                    <Input
                        type="text"
                        placeholder="검색어 입력"
                        className="w-full sm:flex-1 sm:min-w-[300px]"
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                        onKeyDown={(e) => e.key === "Enter" && handleSearch()}
                    />
                    <Button variant="default" onClick={handleSearch}>검색</Button>
                    <Button variant="outline" onClick={handleReset}>초기화</Button>
                </div>
            </div>

            {/* 사용자 목록 */}
            <div className="w-full rounded-lg border mt-4 overflow-x-auto">
                <Table className="w-full">
                    <TableHeader className="bg-muted">
                        <TableRow>
                            <TableHead className="border-r font-semibold w-[60px] text-center">ID</TableHead>
                            <TableHead className="border-r font-semibold w-[150px]">아이디</TableHead>
                            <TableHead className="border-r font-semibold w-[150px]">이름</TableHead>
                            <TableHead className="border-r font-semibold">이메일</TableHead>
                            <TableHead className="border-r font-semibold w-[80px] text-center">역할</TableHead>
                            <TableHead className="border-r font-semibold w-[180px] text-center">마지막 로그인</TableHead>
                            <TableHead className="border-r font-semibold w-[120px] text-center">가입일</TableHead>
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
                        ) : !usersData?.content || usersData.content.length === 0 ? (
                            <TableRow>
                                <TableCell colSpan={8} className="text-center py-8 text-muted-foreground">
                                    등록된 사용자가 없습니다.
                                </TableCell>
                            </TableRow>
                        ) : (
                            usersData.content.map((user) => (
                                <TableRow key={user.id}>
                                    {/* ID */}
                                    <TableCell className="border-r text-center">{user.id}</TableCell>

                                    {/* 아이디 */}
                                    <TableCell className="border-r font-medium">{user.username}</TableCell>

                                    {/* 이름 */}
                                    <TableCell className="border-r">{user.name}</TableCell>

                                    {/* 이메일 */}
                                    <TableCell className="border-r">{user.email}</TableCell>

                                    {/* 역할 */}
                                    <TableCell className="border-r text-center">
                                        <Badge variant={getRoleVariant(user.role)}>
                                            {getRoleLabel(user.role)}
                                        </Badge>
                                    </TableCell>

                                    {/* 마지막 로그인 */}
                                    <TableCell className="border-r text-center text-sm">
                                        {formatDateTime(user.lastLoginAt)}
                                    </TableCell>

                                    {/* 가입일 */}
                                    <TableCell className="border-r text-center">{formatDate(user.createdAt)}</TableCell>

                                    {/* 작업 (수정/삭제) */}
                                    <TableCell className="text-center">
                                        <div className="flex gap-2 justify-center">
                                            <Button
                                                variant="secondary"
                                                size="icon"
                                                onClick={() => handleOpenEditDialog(user)}
                                                disabled={updateMutation.isPending}
                                            >
                                                <Edit />
                                            </Button>
                                            <Button
                                                variant="secondary"
                                                size="icon"
                                                onClick={() => handleDelete(user)}
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
            {usersData && (
                <CustomPagination
                    page={page}
                    totalPages={usersData.totalPages || 0}
                    onPageChange={setPage}
                />
            )}

            {/* 사용자 수정 다이얼로그 */}
            <UserFormDialog
                open={isDialogOpen}
                onOpenChange={handleDialogClose}
                user={selectedUser}
                onSubmit={handleDialogSubmit}
                isSubmitting={updateMutation.isPending}
            />
        </div>
    );
}