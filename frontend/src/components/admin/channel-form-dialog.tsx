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
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Loader2 } from "lucide-react";
import { useState, useEffect } from "react";
import type { AdminChannelResponse, AdminChannelCreateRequestContentPrivacy } from "@/lib/api/models";

interface ChannelFormDialogProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    mode: "create" | "edit";
    channel: AdminChannelResponse | null;
    onSubmit: (data: { name: string; contentPrivacy: AdminChannelCreateRequestContentPrivacy }) => Promise<void>;
    isSubmitting: boolean;
}

export function ChannelFormDialog({
    open,
    onOpenChange,
    mode,
    channel,
    onSubmit,
    isSubmitting,
}: ChannelFormDialogProps) {
    const [formName, setFormName] = useState("");
    const [formPrivacy, setFormPrivacy] = useState<AdminChannelCreateRequestContentPrivacy>("PUBLIC");

    // 다이얼로그가 열릴 때마다 폼 초기화
    useEffect(() => {
        if (open) {
            if (mode === "edit" && channel) {
                setFormName(channel.name);
                setFormPrivacy(channel.contentPrivacy as AdminChannelCreateRequestContentPrivacy);
            } else {
                setFormName("");
                setFormPrivacy("PUBLIC");
            }
        }
    }, [open, mode, channel]);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        await onSubmit({
            name: formName,
            contentPrivacy: formPrivacy,
        });
    };

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="sm:max-w-100">
                <form onSubmit={handleSubmit} className="flex flex-col gap-4">
                    <DialogHeader>
                        <DialogTitle>{mode === "create" ? "채널 생성" : "채널 수정"}</DialogTitle>
                        <DialogDescription>
                            {mode === "create" ? "새로운 채널을 생성합니다." : "채널 정보를 수정합니다."}
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
                        <Button type="submit" disabled={isSubmitting}>
                            {isSubmitting ? (
                                <>
                                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                                    처리중...
                                </>
                            ) : mode === "create" ? (
                                "생성"
                            ) : (
                                "수정"
                            )}
                        </Button>
                    </DialogFooter>
                </form>
            </DialogContent>
        </Dialog>
    );
}