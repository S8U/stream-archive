"use client";

import {
    Dialog,
    DialogClose,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Loader2 } from "lucide-react";
import { useState, useEffect } from "react";
import type { AdminUserResponse, AdminUserUpdateRequestRole } from "@/lib/api/models";

interface UserFormDialogProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    user: AdminUserResponse | null;
    onSubmit: (data: { role: AdminUserUpdateRequestRole }) => Promise<void>;
    isSubmitting: boolean;
}

export function UserFormDialog({
    open,
    onOpenChange,
    user,
    onSubmit,
    isSubmitting,
}: UserFormDialogProps) {
    const [formRole, setFormRole] = useState<AdminUserUpdateRequestRole>("USER");

    // 다이얼로그가 열릴 때마다 폼 초기화
    useEffect(() => {
        if (open && user) {
            setFormRole(user.role as AdminUserUpdateRequestRole);
        }
    }, [open, user]);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        await onSubmit({
            role: formRole,
        });
    };

    if (!user) return null;

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="sm:max-w-100">
                <form onSubmit={handleSubmit} className="flex flex-col gap-4">
                    <DialogHeader>
                        <DialogTitle>사용자 역할 변경</DialogTitle>
                        <DialogDescription>
                            사용자의 역할을 변경합니다.
                        </DialogDescription>
                    </DialogHeader>
                    <div className="grid gap-4">
                        <div className="grid gap-3">
                            <Label>사용자명</Label>
                            <div className="px-3 py-2 bg-muted rounded-md text-sm">
                                {user.username}
                            </div>
                        </div>
                        <div className="grid gap-3">
                            <Label>이름</Label>
                            <div className="px-3 py-2 bg-muted rounded-md text-sm">
                                {user.name}
                            </div>
                        </div>
                        <div className="grid gap-3">
                            <Label>이메일</Label>
                            <div className="px-3 py-2 bg-muted rounded-md text-sm">
                                {user.email}
                            </div>
                        </div>
                        <div className="grid gap-3">
                            <Label>역할</Label>
                            <Select value={formRole} onValueChange={(value) => setFormRole(value as AdminUserUpdateRequestRole)}>
                                <SelectTrigger className="w-full">
                                    <SelectValue placeholder="선택" />
                                </SelectTrigger>
                                <SelectContent>
                                    <SelectGroup>
                                        <SelectItem value="ADMIN">관리자</SelectItem>
                                        <SelectItem value="USER">사용자</SelectItem>
                                    </SelectGroup>
                                </SelectContent>
                            </Select>
                        </div>
                    </div>
                    <DialogFooter>
                        <DialogClose asChild>
                            <Button variant="outline" type="button">취소</Button>
                        </DialogClose>
                        <Button type="submit" disabled={isSubmitting}>
                            {isSubmitting ? (
                                <>
                                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                                    처리중...
                                </>
                            ) : (
                                "저장"
                            )}
                        </Button>
                    </DialogFooter>
                </form>
            </DialogContent>
        </Dialog>
    );
}
