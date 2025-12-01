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
import type { AdminVideoResponse, AdminVideoUpdateRequestContentPrivacy } from "@/lib/api/models";

interface VideoFormDialogProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    video: AdminVideoResponse | null;
    onSubmit: (data: { title: string; contentPrivacy: AdminVideoUpdateRequestContentPrivacy }) => Promise<void>;
    isSubmitting: boolean;
}

export function VideoFormDialog({
    open,
    onOpenChange,
    video,
    onSubmit,
    isSubmitting,
}: VideoFormDialogProps) {
    const [formTitle, setFormTitle] = useState("");
    const [formPrivacy, setFormPrivacy] = useState<AdminVideoUpdateRequestContentPrivacy>("PUBLIC");

    // 다이얼로그가 열릴 때마다 폼 초기화
    useEffect(() => {
        if (open && video) {
            setFormTitle(video.title);
            setFormPrivacy(video.contentPrivacy as AdminVideoUpdateRequestContentPrivacy);
        }
    }, [open, video]);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        await onSubmit({
            title: formTitle,
            contentPrivacy: formPrivacy,
        });
    };

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="sm:max-w-100">
                <form onSubmit={handleSubmit} className="flex flex-col gap-4">
                    <DialogHeader>
                        <DialogTitle>동영상 수정</DialogTitle>
                        <DialogDescription>
                            동영상 정보를 수정합니다.
                        </DialogDescription>
                    </DialogHeader>
                    <div className="grid gap-4">
                        <div className="grid gap-3">
                            <Label>제목</Label>
                            <Input
                                type="text"
                                required
                                value={formTitle}
                                onChange={(e) => setFormTitle(e.target.value)}
                            />
                        </div>
                        <div className="grid gap-3">
                            <Label>공개 범위</Label>
                            <Select value={formPrivacy} onValueChange={(value) => setFormPrivacy(value as AdminVideoUpdateRequestContentPrivacy)}>
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
