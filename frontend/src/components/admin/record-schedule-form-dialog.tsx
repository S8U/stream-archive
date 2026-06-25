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
import { Loader2, CalendarIcon } from "lucide-react";
import { useState, useEffect } from "react";
import type {
    RecordScheduleAdminSearchResponse,
    RecordScheduleAdminCreateRequestPlatformType,
    RecordScheduleAdminCreateRequestScheduleType,
    RecordScheduleAdminCreateRequestRecordQuality
} from "@/lib/api/models";
import { useSearchAdminChannels } from "@/lib/api/endpoints/channel-admin/channel-admin";
import { Checkbox } from "@/components/ui/checkbox";
import { Switch } from "@/components/ui/switch";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { Calendar } from "@/components/ui/calendar";
import { cn } from "@/lib/utils";
import { format } from "date-fns";
import { ko } from "date-fns/locale";
import { toast } from "sonner";

interface RecordScheduleFormDialogProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    mode: "create" | "edit";
    schedule: RecordScheduleAdminSearchResponse | null;
    onSubmit: (data: {
        channelId: number;
        platformType: RecordScheduleAdminCreateRequestPlatformType;
        scheduleType: RecordScheduleAdminCreateRequestScheduleType;
        value: string;
        recordQuality: RecordScheduleAdminCreateRequestRecordQuality;
        priority: number;
        autoArchive: boolean;
    }) => Promise<void>;
    isSubmitting: boolean;
}

const DAYS_OF_WEEK = [
    { label: "월요일", value: "MONDAY" },
    { label: "화요일", value: "TUESDAY" },
    { label: "수요일", value: "WEDNESDAY" },
    { label: "목요일", value: "THURSDAY" },
    { label: "금요일", value: "FRIDAY" },
    { label: "토요일", value: "SATURDAY" },
    { label: "일요일", value: "SUNDAY" },
];

export function RecordScheduleFormDialog({
    open,
    onOpenChange,
    mode,
    schedule,
    onSubmit,
    isSubmitting,
}: RecordScheduleFormDialogProps) {
    const [channelId, setChannelId] = useState<string>("");
    const [platformType, setPlatformType] = useState<RecordScheduleAdminCreateRequestPlatformType>("CHZZK");
    const [scheduleType, setScheduleType] = useState<RecordScheduleAdminCreateRequestScheduleType>("ONCE");
    const [recordQuality, setRecordQuality] = useState<RecordScheduleAdminCreateRequestRecordQuality>("BEST");
    const [priority, setPriority] = useState<number>(0);
    const [autoArchive, setAutoArchive] = useState<boolean>(false);

    // Value states
    const [selectedDays, setSelectedDays] = useState<string[]>([]);
    const [selectedDates, setSelectedDates] = useState<Date[]>([]);

    // Fetch channels for the dropdown
    const { data: channelsData } = useSearchAdminChannels(
        { request: {}, pageable: { page: 0, size: 100 } },
        { query: { enabled: open && mode === "create" } }
    );

    // Reset form on open
    useEffect(() => {
        if (open) {
            if (mode === "edit" && schedule) {
                setChannelId(schedule.channel.id.toString());
                setPlatformType(schedule.platformType as RecordScheduleAdminCreateRequestPlatformType);
                setScheduleType(schedule.scheduleType as RecordScheduleAdminCreateRequestScheduleType);
                setRecordQuality(schedule.recordQuality as RecordScheduleAdminCreateRequestRecordQuality);
                setPriority(schedule.priority);
                setAutoArchive(schedule.autoArchive);

                // Parse value based on schedule type
                if (schedule.scheduleType === "N_DAYS_OF_EVERY_WEEK") {
                    try {
                        const parsed = JSON.parse(schedule.value);
                        if (Array.isArray(parsed)) {
                            setSelectedDays(parsed);
                        }
                    } catch (e) {
                        setSelectedDays([]);
                    }
                } else if (schedule.scheduleType === "SPECIFIC_DAY") {
                    try {
                        const parsed = JSON.parse(schedule.value);
                        if (Array.isArray(parsed)) {
                            setSelectedDates(parsed.map((d: string) => new Date(d)));
                        }
                    } catch (e) {
                        setSelectedDates([]);
                    }
                } else {
                    setSelectedDays([]);
                    setSelectedDates([]);
                }
            } else {
                setChannelId("");
                setPlatformType("CHZZK");
                setScheduleType("ONCE");
                setRecordQuality("BEST");
                setPriority(0);
                setAutoArchive(false);
                setSelectedDays([]);
                setSelectedDates([]);
            }
        }
    }, [open, mode, schedule]);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        // 채널 미선택 방어 (Radix Select는 native required가 안 먹어 빈 값으로 제출될 수 있다)
        if (mode === "create" && !channelId) {
            toast.error("채널을 선택해주세요.");
            return;
        }

        // 스케줄 값 미입력 방어
        if (scheduleType === "N_DAYS_OF_EVERY_WEEK" && selectedDays.length === 0) {
            toast.error("요일을 선택해주세요.");
            return;
        }
        if (scheduleType === "SPECIFIC_DAY" && selectedDates.length === 0) {
            toast.error("날짜를 선택해주세요.");
            return;
        }

        let value = "";
        if (scheduleType === "N_DAYS_OF_EVERY_WEEK") {
            value = JSON.stringify(selectedDays);
        } else if (scheduleType === "SPECIFIC_DAY") {
            value = JSON.stringify(selectedDates.map(d => format(d, "yyyy-MM-dd")));
        }

        await onSubmit({
            channelId: Number(channelId),
            platformType,
            scheduleType,
            value,
            recordQuality,
            priority,
            autoArchive,
        });
    };

    const toggleDay = (day: string) => {
        if (selectedDays.includes(day)) {
            setSelectedDays(selectedDays.filter(d => d !== day));
        } else {
            setSelectedDays([...selectedDays, day]);
        }
    };

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="sm:max-w-[500px]">
                <form onSubmit={handleSubmit} className="flex flex-col gap-4">
                    <DialogHeader>
                        <DialogTitle>{mode === "create" ? "녹화 스케줄 생성" : "녹화 스케줄 수정"}</DialogTitle>
                        <DialogDescription>
                            {mode === "create" ? "새로운 녹화 스케줄을 생성합니다." : "녹화 스케줄 정보를 수정합니다."}
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
                                <Input value={schedule?.channel.name || ""} disabled />
                            )}
                        </div>

                        {/* 플랫폼 유형 */}
                        <div className="grid gap-3">
                            <Label>플랫폼</Label>
                            <Select
                                value={platformType}
                                onValueChange={(value) => setPlatformType(value as RecordScheduleAdminCreateRequestPlatformType)}
                            >
                                <SelectTrigger className="w-full">
                                    <SelectValue placeholder="플랫폼 선택" />
                                </SelectTrigger>
                                <SelectContent>
                                    <SelectGroup>
                                        <SelectItem value="CHZZK">치지직</SelectItem>
                                        <SelectItem value="TWITCH">트위치</SelectItem>
                                        <SelectItem value="SOOP">SOOP</SelectItem>
                                        <SelectItem value="YOUTUBE">유튜브</SelectItem>
                                    </SelectGroup>
                                </SelectContent>
                            </Select>
                        </div>

                        {/* 스케줄 유형 */}
                        <div className="grid gap-3">
                            <Label>스케줄 유형</Label>
                            <Select
                                value={scheduleType}
                                onValueChange={(value) => setScheduleType(value as RecordScheduleAdminCreateRequestScheduleType)}
                            >
                                <SelectTrigger className="w-full">
                                    <SelectValue placeholder="스케줄 유형 선택" />
                                </SelectTrigger>
                                <SelectContent>
                                    <SelectGroup>
                                        <SelectItem value="ONCE">한 번만</SelectItem>
                                        <SelectItem value="ALWAYS">항상</SelectItem>
                                        <SelectItem value="N_DAYS_OF_EVERY_WEEK">매주 N요일</SelectItem>
                                        <SelectItem value="SPECIFIC_DAY">날짜 지정</SelectItem>
                                    </SelectGroup>
                                </SelectContent>
                            </Select>
                        </div>

                        {/* 스케줄 값 (요일/날짜 선택) */}
                        {scheduleType === "N_DAYS_OF_EVERY_WEEK" && (
                            <div className="grid gap-3">
                                <Label>요일 선택</Label>
                                <div className="grid grid-cols-4 gap-2">
                                    {DAYS_OF_WEEK.map((day) => (
                                        <div key={day.value} className="flex items-center space-x-2">
                                            <Checkbox
                                                id={`day-${day.value}`}
                                                checked={selectedDays.includes(day.value)}
                                                onCheckedChange={() => toggleDay(day.value)}
                                            />
                                            <Label htmlFor={`day-${day.value}`} className="text-sm font-normal cursor-pointer">
                                                {day.label}
                                            </Label>
                                        </div>
                                    ))}
                                </div>
                            </div>
                        )}

                        {scheduleType === "SPECIFIC_DAY" && (
                            <div className="grid gap-3">
                                <Label>날짜 선택</Label>
                                <Popover>
                                    <PopoverTrigger asChild>
                                        <Button
                                            variant={"outline"}
                                            className={cn(
                                                "w-full justify-start text-left font-normal",
                                                !selectedDates.length && "text-muted-foreground"
                                            )}
                                        >
                                            <CalendarIcon className="mr-2 h-4 w-4" />
                                            {selectedDates.length > 0 ? (
                                                `${selectedDates.length}개 날짜 선택됨`
                                            ) : (
                                                <span>날짜 선택</span>
                                            )}
                                        </Button>
                                    </PopoverTrigger>
                                    <PopoverContent className="w-auto p-0" align="start">
                                        <Calendar
                                            mode="multiple"
                                            selected={selectedDates}
                                            onSelect={(dates) => setSelectedDates(dates ?? [])}
                                            locale={ko}
                                        />
                                    </PopoverContent>
                                </Popover>
                                {selectedDates.length > 0 && (
                                    <div className="flex flex-wrap gap-1 mt-2">
                                        {selectedDates.sort((a, b) => a.getTime() - b.getTime()).map((date) => (
                                            <div key={date.toISOString()} className="bg-secondary text-secondary-foreground px-2 py-1 rounded-md text-xs">
                                                {format(date, "yyyy-MM-dd")}
                                            </div>
                                        ))}
                                    </div>
                                )}
                            </div>
                        )}

                        {/* 녹화 품질 */}
                        <div className="grid gap-3">
                            <Label>녹화 품질</Label>
                            <Select
                                value={recordQuality}
                                onValueChange={(value) => setRecordQuality(value as RecordScheduleAdminCreateRequestRecordQuality)}
                            >
                                <SelectTrigger className="w-full">
                                    <SelectValue placeholder="녹화 품질 선택" />
                                </SelectTrigger>
                                <SelectContent>
                                    <SelectGroup>
                                        <SelectItem value="BEST">최고 화질</SelectItem>
                                        <SelectItem value="WORST">최저 화질</SelectItem>
                                    </SelectGroup>
                                </SelectContent>
                            </Select>
                        </div>

                        {/* 우선순위 */}
                        <div className="grid gap-3">
                            <Label>우선순위</Label>
                            <div className="grid gap-1.5">
                                <Input
                                    type="number"
                                    value={priority}
                                    onChange={(e) => setPriority(Number(e.target.value))}
                                    placeholder="0"
                                />
                                <p className="text-xs text-muted-foreground">높을수록 우선순위가 높습니다.</p>
                            </div>
                        </div>

                        {/* 자동 소장 */}
                        <div className="flex items-center justify-between gap-4">
                            <div className="grid gap-1">
                                <Label htmlFor="auto-archive">자동 소장</Label>
                                <p className="text-xs text-muted-foreground">
                                    이 스케줄로 녹화된 동영상을 자동으로 소장합니다.
                                </p>
                            </div>
                            <Switch
                                id="auto-archive"
                                checked={autoArchive}
                                onCheckedChange={setAutoArchive}
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
