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
import { Switch } from "@/components/ui/switch";
import { Loader2 } from "lucide-react";
import { useState, useEffect } from "react";
import type { AdminChannelPlatformResponse, AdminChannelPlatformCreateRequestPlatformType } from "@/lib/api/models";
import { useSearchAdminChannels } from "@/lib/api/endpoints/admin-channel/admin-channel";

interface ChannelPlatformFormDialogProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    mode: "create" | "edit";
    platform: AdminChannelPlatformResponse | null;
    onSubmit: (data: {
        channelId: number;
        platformType: AdminChannelPlatformCreateRequestPlatformType;
        platformChannelId: string;
        isSyncProfile: boolean;
    }) => Promise<void>;
    isSubmitting: boolean;
}

export function ChannelPlatformFormDialog({
    open,
    onOpenChange,
    mode,
    platform,
    onSubmit,
    isSubmitting,
}: ChannelPlatformFormDialogProps) {
    const [channelId, setChannelId] = useState<string>("");
    const [platformType, setPlatformType] = useState<AdminChannelPlatformCreateRequestPlatformType>("CHZZK");
    const [platformChannelId, setPlatformChannelId] = useState("");
    const [isSyncProfile, setIsSyncProfile] = useState(false);

    // Fetch channels for the dropdown
    const { data: channelsData } = useSearchAdminChannels(
        { request: {}, pageable: { page: 0, size: 100 } },
        { query: { enabled: open && mode === "create" } }
    );

    // Reset form on open
    useEffect(() => {
        if (open) {
            if (mode === "edit" && platform) {
                setChannelId(platform.channel.id.toString());
                setPlatformType(platform.platformType as AdminChannelPlatformCreateRequestPlatformType);
                setPlatformChannelId(platform.platformChannelId);
                setIsSyncProfile(platform.isSyncProfile);
            } else {
                setChannelId("");
                setPlatformType("CHZZK");
                setPlatformChannelId("");
                setIsSyncProfile(false);
            }
        }
    }, [open, mode, platform]);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        await onSubmit({
            channelId: Number(channelId),
            platformType,
            platformChannelId,
            isSyncProfile,
        });
    };

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="sm:max-w-[500px]">
                <form onSubmit={handleSubmit} className="flex flex-col gap-4">
                    <DialogHeader>
                        <DialogTitle>{mode === "create" ? "플랫폼 연결 생성" : "플랫폼 연결 수정"}</DialogTitle>
                        <DialogDescription>
                            {mode === "create" ? "새로운 플랫폼 연결을 생성합니다." : "플랫폼 연결 정보를 수정합니다."}
                        </DialogDescription>
                    </DialogHeader>
                    <div className="grid gap-4">
                        {/* 채널 선택 - 생성 모드에서만 */}
                        <div className="grid gap-3">
                            <Label>채널</Label>
                            {mode === "create" ? (
                                <Select value={channelId} onValueChange={setChannelId} required>
                                    <SelectTrigger className="w-full">
                                        <SelectValue placeholder="채널 선택" />
                                    </SelectTrigger>
                                    <SelectContent>
                                        <SelectGroup>
                                            {channelsData?.content?.map((channel) => (
                                                <SelectItem key={channel.id} value={channel.id.toString()}>
                                                    {channel.name}
                                                </SelectItem>
                                            ))}
                                        </SelectGroup>
                                    </SelectContent>
                                </Select>
                            ) : (
                                <Input value={channelId} disabled />
                            )}
                        </div>

                        {/* 플랫폼 유형 */}
                        <div className="grid gap-3">
                            <Label>플랫폼</Label>
                            <Select
                                value={platformType}
                                onValueChange={(value) => setPlatformType(value as AdminChannelPlatformCreateRequestPlatformType)}
                                disabled={mode === "edit"}
                            >
                                <SelectTrigger className="w-full">
                                    <SelectValue placeholder="플랫폼 선택" />
                                </SelectTrigger>
                                <SelectContent>
                                    <SelectGroup>
                                        <SelectItem value="CHZZK">치지직</SelectItem>
                                        <SelectItem value="TWITCH">트위치</SelectItem>
                                        <SelectItem value="SOOP">숲</SelectItem>
                                    </SelectGroup>
                                </SelectContent>
                            </Select>
                        </div>

                        {/* 플랫폼 채널 ID */}
                        <div className="grid gap-3">
                            <Label>플랫폼 채널 ID</Label>
                            <Input
                                type="text"
                                required
                                value={platformChannelId}
                                onChange={(e) => setPlatformChannelId(e.target.value)}
                                placeholder="예: 123456"
                            />
                        </div>

                        {/* 프로필 동기화 */}
                        <div className="flex items-center justify-between space-x-2">
                            <Label htmlFor="sync-profile">프로필 동기화</Label>
                            <Switch
                                id="sync-profile"
                                checked={isSyncProfile}
                                onCheckedChange={setIsSyncProfile}
                            />
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
