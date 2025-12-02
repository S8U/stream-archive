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
    AdminRecordScheduleResponse,
    AdminRecordScheduleCreateRequestPlatformType,
    AdminRecordScheduleCreateRequestScheduleType,
    AdminRecordScheduleCreateRequestRecordQuality
} from "@/lib/api/models";
import { useSearchAdminChannels } from "@/lib/api/endpoints/admin-channel/admin-channel";
import { Checkbox } from "@/components/ui/checkbox";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { Calendar } from "@/components/ui/calendar";
import { cn } from "@/lib/utils";
import { format } from "date-fns";
import { ko } from "date-fns/locale";

interface RecordScheduleFormDialogProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    mode: "create" | "edit";
    schedule: AdminRecordScheduleResponse | null;
    onSubmit: (data: {
        channelId: number;
        platformType: AdminRecordScheduleCreateRequestPlatformType;
        scheduleType: AdminRecordScheduleCreateRequestScheduleType;
        value: string;
        recordQuality: AdminRecordScheduleCreateRequestRecordQuality;
        priority: number;
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
    const [platformType, setPlatformType] = useState<AdminRecordScheduleCreateRequestPlatformType>("CHZZK");
    const [scheduleType, setScheduleType] = useState<AdminRecordScheduleCreateRequestScheduleType>("ONCE");
    const [recordQuality, setRecordQuality] = useState<AdminRecordScheduleCreateRequestRecordQuality>("BEST");
    const [priority, setPriority] = useState<number>(0);

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
                setPlatformType(schedule.platformType as AdminRecordScheduleCreateRequestPlatformType);
                setScheduleType(schedule.scheduleType as AdminRecordScheduleCreateRequestScheduleType);
                setRecordQuality(schedule.recordQuality as AdminRecordScheduleCreateRequestRecordQuality);
                setPriority(schedule.priority);

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
                setSelectedDays([]);
                setSelectedDates([]);
            }
        }
    }, [open, mode, schedule]);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

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
                                onValueChange={(value) => setPlatformType(value as AdminRecordScheduleCreateRequestPlatformType)}
                            >
                                <SelectTrigger className="w-full">
                                    <SelectValue placeholder="플랫폼 선택" />
                                </SelectTrigger>
                                <SelectContent>
                                    <SelectGroup>
                                        <SelectItem value="CHZZK">치지직</SelectItem>
                                        <SelectItem value="TWITCH">트위치</SelectItem>
                                        <SelectItem value="SOOP">SOOP</SelectItem>
                                    </SelectGroup>
                                </SelectContent>
                            </Select>
                        </div>

                        {/* 스케줄 유형 */}
                        <div className="grid gap-3">
                            <Label>스케줄 유형</Label>
                            <Select
                                value={scheduleType}
                                onValueChange={(value) => setScheduleType(value as AdminRecordScheduleCreateRequestScheduleType)}
                            >
                                <SelectTrigger className="w-full">
                                    <SelectValue placeholder="스케줄 유형 선택" />
                                </SelectTrigger>
                                <SelectContent>
                                    <SelectGroup>
                                        <SelectItem value="ONCE">한 번만</SelectItem>
                                        <SelectItem value="ALWAYS">항상</SelectItem>
                                        <SelectItem value="N_DAYS_OF_EVERY_WEEK">매주 n요일</SelectItem>
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
                                            onSelect={setSelectedDates}
                                            initialFocus
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
                                onValueChange={(value) => setRecordQuality(value as AdminRecordScheduleCreateRequestRecordQuality)}
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
                            <Input
                                type="number"
                                value={priority}
                                onChange={(e) => setPriority(Number(e.target.value))}
                                placeholder="0"
                            />
                            <p className="text-xs text-muted-foreground">높을수록 우선순위가 높습니다.</p>
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
