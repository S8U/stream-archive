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
import { Switch } from "@/components/ui/switch";
import { cn } from "@/lib/utils";
import { Loader2 } from "lucide-react";
import { useState, useEffect } from "react";
import { MIN_DAYS, MAX_DAYS, DAY_PRESETS as PRESETS } from "@/app/admin/auto-delete/constants";

interface ChannelPolicyValue {
    isEnabled: boolean;
    deleteAfterDays: number;
}

interface AutoDeleteChannelPolicyDialogProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    channelName: string;
    // 전체 기본 정책 일수 (전체 기본값 따름 모드일 때 안내용)
    globalDays: number | null;
    // 기존 채널별 정책 (없으면 전체 기본값을 따르는 상태)
    initialValue: ChannelPolicyValue | null;
    onSubmit: (value: ChannelPolicyValue) => void;
    onResetToGlobal: () => void;
    isSubmitting: boolean;
}

export function AutoDeleteChannelPolicyDialog({
    open,
    onOpenChange,
    channelName,
    globalDays,
    initialValue,
    onSubmit,
    onResetToGlobal,
    isSubmitting,
}: AutoDeleteChannelPolicyDialogProps) {
    // "default" = 전체 기본값 따름, "custom" = 이 채널만 설정
    const [mode, setMode] = useState<"default" | "custom">("default");
    const [isEnabled, setIsEnabled] = useState(true);
    const [deleteAfterDays, setDeleteAfterDays] = useState("30");

    useEffect(() => {
        if (open) {
            setMode(initialValue ? "custom" : "default");
            setIsEnabled(initialValue?.isEnabled ?? true);
            setDeleteAfterDays(initialValue?.deleteAfterDays?.toString() ?? "30");
        }
    }, [open, initialValue]);

    const days = Number(deleteAfterDays);
    const stepDays = (delta: number) => {
        const next = Math.min(MAX_DAYS, Math.max(MIN_DAYS, (Number.isInteger(days) ? days : 30) + delta));
        setDeleteAfterDays(next.toString());
    };

    // 일수 직접 입력은 다 친 뒤(blur) MIN_DAYS~MAX_DAYS로 보정한다
    const commitDays = () => {
        const next = Math.min(MAX_DAYS, Math.max(MIN_DAYS, Number.isInteger(days) ? days : 30));
        setDeleteAfterDays(next.toString());
    };

    const handleSubmit = () => {
        if (mode === "default") {
            onResetToGlobal();
            return;
        }

        // 입력값을 범위로 보정해 화면·저장을 함께 맞춘다
        const safeDays = Math.min(MAX_DAYS, Math.max(MIN_DAYS, Number.isInteger(days) ? days : 30));
        setDeleteAfterDays(safeDays.toString());

        onSubmit({ isEnabled, deleteAfterDays: safeDays });
    };

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent>
                <DialogHeader>
                    <DialogTitle>{channelName}</DialogTitle>
                    <DialogDescription>이 채널의 자동 삭제 정책을 설정합니다.</DialogDescription>
                </DialogHeader>

                <div className="flex flex-col gap-6 py-2">
                    {/* 모드 선택 */}
                    <div className="grid gap-2">
                        <span className="text-sm font-medium">삭제 정책</span>
                        <div className="grid grid-cols-2 gap-2">
                            <button
                                type="button"
                                onClick={() => setMode("default")}
                                className={cn(
                                    "rounded-lg border p-3 text-left transition-colors",
                                    mode === "default"
                                        ? "border-primary bg-primary/5"
                                        : "border-border hover:bg-muted"
                                )}
                            >
                                <div className="text-sm font-semibold">전체 기본값 따름</div>
                                <div className="mt-1 text-xs text-muted-foreground">
                                    기본 {globalDays ?? "-"}일
                                </div>
                            </button>
                            <button
                                type="button"
                                onClick={() => setMode("custom")}
                                className={cn(
                                    "rounded-lg border p-3 text-left transition-colors",
                                    mode === "custom"
                                        ? "border-primary bg-primary/5"
                                        : "border-border hover:bg-muted"
                                )}
                            >
                                <div className="text-sm font-semibold">이 채널만 설정</div>
                                <div className="mt-1 text-xs text-muted-foreground">개별 기준 지정</div>
                            </button>
                        </div>
                    </div>

                    {/* 개별 설정 영역 */}
                    <div
                        className={cn(
                            "flex flex-col gap-5 transition-opacity",
                            mode === "custom" ? "opacity-100" : "pointer-events-none opacity-40"
                        )}
                    >
                        <div className="flex items-center justify-between gap-4 border-t pt-5">
                            <div className="grid gap-1">
                                <Label htmlFor="channel-enabled">자동 삭제</Label>
                                <p className="text-sm text-muted-foreground">
                                    끄면 이 채널은 자동 삭제하지 않습니다.
                                </p>
                            </div>
                            <Switch
                                id="channel-enabled"
                                checked={isEnabled}
                                onCheckedChange={setIsEnabled}
                            />
                        </div>

                        <div
                            className={cn(
                                "grid gap-3 border-t pt-5 transition-opacity",
                                isEnabled ? "opacity-100" : "pointer-events-none opacity-40"
                            )}
                        >
                            <Label className="text-muted-foreground">보관 기간</Label>
                            <div className="flex items-center gap-3">
                                <Button
                                    type="button"
                                    variant="outline"
                                    size="icon"
                                    className="size-10 text-lg"
                                    onClick={() => stepDays(-1)}
                                >
                                    −
                                </Button>
                                <div className="flex items-baseline justify-center gap-1.5 px-2">
                                    <Input
                                        type="number"
                                        min={MIN_DAYS}
                                        max={MAX_DAYS}
                                        className="no-spinner h-11 w-28 text-center !text-2xl font-bold"
                                        value={deleteAfterDays}
                                        onChange={(e) => setDeleteAfterDays(e.target.value)}
                                        onBlur={commitDays}
                                    />
                                    <span className="text-sm text-muted-foreground">일</span>
                                </div>
                                <Button
                                    type="button"
                                    variant="outline"
                                    size="icon"
                                    className="size-10 text-lg"
                                    onClick={() => stepDays(1)}
                                >
                                    +
                                </Button>
                            </div>
                            <div className="flex gap-2">
                                {PRESETS.map((d) => (
                                    <Button
                                        key={d}
                                        type="button"
                                        variant={days === d ? "default" : "outline"}
                                        size="sm"
                                        className="flex-1"
                                        onClick={() => setDeleteAfterDays(d.toString())}
                                    >
                                        {d}일
                                    </Button>
                                ))}
                            </div>
                        </div>
                    </div>
                </div>

                <DialogFooter>
                    <DialogClose asChild>
                        <Button variant="outline">취소</Button>
                    </DialogClose>
                    <Button onClick={handleSubmit} disabled={isSubmitting}>
                        {isSubmitting ? <Loader2 className="animate-spin" /> : null}
                        완료
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}
