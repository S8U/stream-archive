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
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Checkbox } from "@/components/ui/checkbox";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { Calendar } from "@/components/ui/calendar";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { PlatformBadge } from "@/components/common/platform-badge";
import { Loader2, CalendarIcon, AlertCircle } from "lucide-react";
import { AnimatePresence, motion, useReducedMotion } from "motion/react";
import { useState, useEffect, useRef } from "react";
import type {
    ChannelAdminQuickCreateRequest,
    ChannelAdminQuickCreateRequestContentPrivacy,
    ChannelAdminQuickCreateRequestPlatformType,
    ScheduleRequestScheduleType,
    ScheduleRequestRecordQuality,
} from "@/lib/api/models";
import { useResolveAdminPlatformChannel } from "@/lib/api/endpoints/platform-admin/platform-admin";
import { cn } from "@/lib/utils";
import { format } from "date-fns";
import { ko } from "date-fns/locale";
import { toast } from "sonner";

interface QuickChannelAddDialogProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    onSubmit: (data: ChannelAdminQuickCreateRequest) => Promise<void>;
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

// URL 입력이 멈춘 뒤 채널 정보를 불러오기까지 기다리는 시간
const RESOLVE_DEBOUNCE_MS = 600;

export function QuickChannelAddDialog({
    open,
    onOpenChange,
    onSubmit,
    isSubmitting,
}: QuickChannelAddDialogProps) {
    // 동작 줄이기 설정이면 애니메이션을 즉시 끝낸다 (OS 접근성 설정 존중)
    const reduceMotion = useReducedMotion();

    // 높이 변화에 쓰는 iOS풍 스프링
    const heightSpring = reduceMotion
        ? { duration: 0 }
        : { type: "spring" as const, stiffness: 380, damping: 34 };

    // 영역이 펼쳐질 때 높이까지 부드럽게 자라게 한다
    const collapseProps = {
        initial: { height: 0, opacity: 0 },
        animate: { height: "auto" as const, opacity: 1 },
        exit: { height: 0, opacity: 0 },
        transition: reduceMotion
            ? { duration: 0 }
            : { ...heightSpring, opacity: { duration: 0.18 } }
    };

    // 탭 전환으로 활성 패널 높이가 바뀔 때 그 높이를 측정해 부드럽게 따라간다
    // (Radix 탭은 패널을 교체해 motion의 layout이 안 먹으므로 직접 측정한다)
    const [tabBodyHeight, setTabBodyHeight] = useState<number | "auto">("auto");
    const observerRef = useRef<ResizeObserver | null>(null);

    // 콜백 ref: 탭 본문이 마운트되면 높이를 관찰하고, 사라지면 정리한다
    const tabBodyRef = (el: HTMLDivElement | null) => {
        observerRef.current?.disconnect();
        if (!el) return;

        setTabBodyHeight(el.offsetHeight);
        const observer = new ResizeObserver(() => setTabBodyHeight(el.offsetHeight));
        observer.observe(el);
        observerRef.current = observer;
    };

    // URL 입력과 디바운스된 URL (디바운스된 값으로만 조회한다)
    const [url, setUrl] = useState("");
    const [debouncedUrl, setDebouncedUrl] = useState("");

    // 채널 기본 정보
    const [name, setName] = useState("");
    const [contentPrivacy, setContentPrivacy] = useState<ChannelAdminQuickCreateRequestContentPrivacy>("PUBLIC");
    const [isSyncProfile, setIsSyncProfile] = useState(true);

    // 인식된 플랫폼 정보 (조회 성공 시 채워짐)
    const [resolvedPlatformType, setResolvedPlatformType] = useState<ChannelAdminQuickCreateRequestPlatformType | null>(null);
    const [resolvedChannelId, setResolvedChannelId] = useState("");
    const [thumbnailUrl, setThumbnailUrl] = useState<string | undefined>(undefined);

    // 스케줄 설정 (기본은 항상 녹화, 끄면 채널·플랫폼만 만든다)
    const [scheduleEnabled, setScheduleEnabled] = useState(true);
    const [scheduleType, setScheduleType] = useState<ScheduleRequestScheduleType>("ALWAYS");
    const [recordQuality, setRecordQuality] = useState<ScheduleRequestRecordQuality>("BEST");
    const [priority, setPriority] = useState<number>(0);
    const [autoArchive, setAutoArchive] = useState<boolean>(false);
    const [selectedDays, setSelectedDays] = useState<string[]>([]);
    const [selectedDates, setSelectedDates] = useState<Date[]>([]);

    // 다이얼로그 열릴 때 폼 초기화
    useEffect(() => {
        if (open) {
            setUrl("");
            setDebouncedUrl("");
            setName("");
            setContentPrivacy("PUBLIC");
            setIsSyncProfile(true);
            setResolvedPlatformType(null);
            setResolvedChannelId("");
            setThumbnailUrl(undefined);
            setScheduleEnabled(true);
            setScheduleType("ALWAYS");
            setRecordQuality("BEST");
            setPriority(0);
            setAutoArchive(false);
            setSelectedDays([]);
            setSelectedDates([]);
        }
    }, [open]);

    // URL 입력이 멈추면 디바운스된 URL을 갱신한다
    useEffect(() => {
        const trimmed = url.trim();
        if (!trimmed) {
            setDebouncedUrl("");
            return;
        }

        const timer = setTimeout(() => {
            setDebouncedUrl(trimmed);
        }, RESOLVE_DEBOUNCE_MS);

        return () => clearTimeout(timer);
    }, [url]);

    // 디바운스된 URL로 플랫폼 채널 정보 조회
    const {
        data: resolveData,
        isFetching: isResolving,
        isError: isResolveError,
    } = useResolveAdminPlatformChannel(
        { request: { url: debouncedUrl } },
        { query: { enabled: open && debouncedUrl.length > 0, retry: false } }
    );

    // 조회 결과를 폼에 반영한다 (이름은 비어 있을 때만 자동 채워 사용자가 고친 값을 덮어쓰지 않는다)
    useEffect(() => {
        if (resolveData) {
            setResolvedPlatformType(resolveData.platformType as ChannelAdminQuickCreateRequestPlatformType);
            setResolvedChannelId(resolveData.platformChannelId);
            setThumbnailUrl(resolveData.thumbnailUrl);
            setName((prev) => (prev.trim() ? prev : resolveData.name));
        }
    }, [resolveData]);

    // 조회 실패하면 인식 정보를 비운다
    useEffect(() => {
        if (isResolveError) {
            setResolvedPlatformType(null);
            setResolvedChannelId("");
            setThumbnailUrl(undefined);
        }
    }, [isResolveError]);

    const handleUrlChange = (value: string) => {
        setUrl(value);
        setName("");
        setResolvedPlatformType(null);
        setResolvedChannelId("");
        setThumbnailUrl(undefined);
    };

    const isResolved = !!resolvedPlatformType && !!resolvedChannelId;

    const toggleDay = (day: string) => {
        if (selectedDays.includes(day)) {
            setSelectedDays(selectedDays.filter((d) => d !== day));
        } else {
            setSelectedDays([...selectedDays, day]);
        }
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        // 플랫폼 인식 방어
        if (!resolvedPlatformType || !resolvedChannelId) {
            toast.error("플랫폼 채널을 먼저 인식해주세요.");
            return;
        }

        // 채널 이름 방어
        if (!name.trim()) {
            toast.error("채널 이름을 입력해주세요.");
            return;
        }

        // 스케줄을 만들 때만 값을 검증하고 인코딩한다
        let schedule: ChannelAdminQuickCreateRequest["schedule"] = undefined;
        if (scheduleEnabled) {
            if (scheduleType === "N_DAYS_OF_EVERY_WEEK" && selectedDays.length === 0) {
                toast.error("요일을 선택해주세요.");
                return;
            }
            if (scheduleType === "SPECIFIC_DAY" && selectedDates.length === 0) {
                toast.error("날짜를 선택해주세요.");
                return;
            }

            let value = "[]";
            if (scheduleType === "N_DAYS_OF_EVERY_WEEK") {
                value = JSON.stringify(selectedDays);
            } else if (scheduleType === "SPECIFIC_DAY") {
                value = JSON.stringify(selectedDates.map((d) => format(d, "yyyy-MM-dd")));
            }

            schedule = {
                scheduleType,
                value,
                recordQuality,
                priority,
                autoArchive,
            };
        }

        await onSubmit({
            name: name.trim(),
            contentPrivacy,
            platformType: resolvedPlatformType,
            platformChannelId: resolvedChannelId,
            isSyncProfile,
            schedule,
        });
    };

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="sm:max-w-[500px] max-h-[90vh] overflow-y-auto">
                <form onSubmit={handleSubmit} className="flex flex-col gap-4">
                    <DialogHeader>
                        <DialogTitle>빠른 채널 추가</DialogTitle>
                        <DialogDescription>
                            URL을 붙여넣어 채널을 추가합니다.
                        </DialogDescription>
                    </DialogHeader>

                    <div className="grid gap-4">
                        {/* 플랫폼 채널 URL */}
                        <div className="grid gap-3">
                            <Label htmlFor="quick-url">플랫폼 채널 URL</Label>
                            <Input
                                id="quick-url"
                                type="text"
                                value={url}
                                onChange={(e) => handleUrlChange(e.target.value)}
                                placeholder="예: https://chzzk.naver.com/live/..."
                                autoComplete="off"
                            />

                            {/* 인식 상태 표시 */}
                            {isResolving ? (
                                <div className="flex items-center gap-2 text-sm text-muted-foreground">
                                    <Loader2 className="h-4 w-4 animate-spin" />
                                    채널 정보를 불러오는 중...
                                </div>
                            ) : resolvedPlatformType ? (
                                // 입력 바로 아래에 뜨므로 슬라이드 대신 제자리에서 떠오르게 한다
                                <div className="flex items-center gap-3 rounded-md border bg-muted/50 p-3 animate-in fade-in zoom-in-95 duration-200 ease-out motion-reduce:animate-none">
                                    <Avatar className="h-11 w-11 shrink-0">
                                        <AvatarImage src={thumbnailUrl} />
                                        <AvatarFallback>{resolveData?.name?.[0]?.toUpperCase()}</AvatarFallback>
                                    </Avatar>
                                    <div className="flex min-w-0 flex-1 flex-col gap-1">
                                        <div className="flex items-center gap-2">
                                            <span className="truncate text-sm font-semibold">{resolveData?.name}</span>
                                            <PlatformBadge platform={resolvedPlatformType} className="shrink-0" />
                                        </div>
                                        <span className="truncate text-xs text-muted-foreground font-mono">{resolvedChannelId}</span>
                                    </div>
                                </div>
                            ) : isResolveError ? (
                                <div className="flex items-center gap-2 text-sm text-destructive">
                                    <AlertCircle className="h-4 w-4" />
                                    채널을 인식할 수 없습니다. URL을 확인해주세요.
                                </div>
                            ) : null}
                        </div>

                        <AnimatePresence initial={false}>
                        {isResolved && (
                            <motion.div {...collapseProps} className="overflow-hidden">
                            {/* 탭 전환으로 본문 높이가 바뀌면 측정값으로 부드럽게 따라간다 */}
                            <motion.div animate={{ height: tabBodyHeight }} transition={heightSpring} className="overflow-hidden">
                            <div ref={tabBodyRef}>
                            <Tabs defaultValue="channel" className="gap-4">
                                <TabsList className="w-full">
                                    <TabsTrigger value="channel">채널</TabsTrigger>
                                    <TabsTrigger value="schedule">녹화 스케줄</TabsTrigger>
                                </TabsList>

                                {/* 채널 탭 */}
                                <TabsContent value="channel" className="grid gap-4">
                                    {/* 채널 이름 (자동 채움 + 수정 가능) */}
                                    <div className="grid gap-3">
                                        <Label htmlFor="quick-name">채널 이름</Label>
                                        <Input
                                            id="quick-name"
                                            type="text"
                                            value={name}
                                            onChange={(e) => setName(e.target.value)}
                                        />
                                    </div>

                                    {/* 공개 범위 */}
                                    <div className="grid gap-3">
                                        <Label>공개 범위</Label>
                                        <Select
                                            value={contentPrivacy}
                                            onValueChange={(value) => setContentPrivacy(value as ChannelAdminQuickCreateRequestContentPrivacy)}
                                        >
                                            <SelectTrigger className="w-full">
                                                <SelectValue placeholder="공개 범위 선택" />
                                            </SelectTrigger>
                                            <SelectContent>
                                                <SelectGroup>
                                                    <SelectItem value="PUBLIC">공개</SelectItem>
                                                    <SelectItem value="UNLISTED">일부공개</SelectItem>
                                                    <SelectItem value="PRIVATE">비공개</SelectItem>
                                                </SelectGroup>
                                            </SelectContent>
                                        </Select>
                                    </div>

                                    {/* 프로필 동기화 */}
                                    <div className="flex items-center justify-between gap-4">
                                        <div className="grid gap-1">
                                            <Label htmlFor="quick-sync-profile">프로필 동기화</Label>
                                            <p className="text-xs text-muted-foreground">
                                                플랫폼의 채널 이미지를 주기적으로 동기화합니다.
                                            </p>
                                        </div>
                                        <Switch
                                            id="quick-sync-profile"
                                            checked={isSyncProfile}
                                            onCheckedChange={setIsSyncProfile}
                                        />
                                    </div>
                                </TabsContent>

                                {/* 녹화 스케줄 탭 */}
                                <TabsContent value="schedule" className="grid gap-4">
                                    {/* 녹화 스케줄 생성 (끄면 채널·플랫폼만 만든다) */}
                                    <div className="flex items-center justify-between gap-4">
                                        <div className="grid gap-1">
                                            <Label htmlFor="quick-schedule-enabled">녹화 스케줄 생성</Label>
                                            <p className="text-xs text-muted-foreground">
                                                이 채널의 방송을 녹화할 스케줄을 함께 등록합니다.
                                            </p>
                                        </div>
                                        <Switch
                                            id="quick-schedule-enabled"
                                            checked={scheduleEnabled}
                                            onCheckedChange={setScheduleEnabled}
                                        />
                                    </div>

                                    {/* 스케줄 설정 (토글이 켜져 있을 때만 전체 표시) */}
                                    <AnimatePresence initial={false}>
                                    {scheduleEnabled && (
                                        <motion.div {...collapseProps} className="overflow-hidden">
                                            {/* gap-4 대신 내부 래퍼에 줘서 height 0일 때 여백이 안 남게 한다 */}
                                            <div className="grid gap-4 pt-1">
                                            {/* 스케줄 유형 */}
                                            <div className="grid gap-3">
                                                <Label>스케줄 유형</Label>
                                                <Select
                                                    value={scheduleType}
                                                    onValueChange={(value) => setScheduleType(value as ScheduleRequestScheduleType)}
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

                                            {/* 요일 선택 */}
                                            {scheduleType === "N_DAYS_OF_EVERY_WEEK" && (
                                                <div className="grid gap-3">
                                                    <Label>요일 선택</Label>
                                                    <div className="grid grid-cols-4 gap-2">
                                                        {DAYS_OF_WEEK.map((day) => (
                                                            <div key={day.value} className="flex items-center space-x-2">
                                                                <Checkbox
                                                                    id={`quick-day-${day.value}`}
                                                                    checked={selectedDays.includes(day.value)}
                                                                    onCheckedChange={() => toggleDay(day.value)}
                                                                />
                                                                <Label htmlFor={`quick-day-${day.value}`} className="text-sm font-normal cursor-pointer">
                                                                    {day.label}
                                                                </Label>
                                                            </div>
                                                        ))}
                                                    </div>
                                                </div>
                                            )}

                                            {/* 날짜 선택 */}
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
                                                                {selectedDates.length > 0 ? `${selectedDates.length}개 날짜 선택됨` : <span>날짜 선택</span>}
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
                                                            {[...selectedDates]
                                                                .sort((a, b) => a.getTime() - b.getTime())
                                                                .map((date) => (
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
                                                    onValueChange={(value) => setRecordQuality(value as ScheduleRequestRecordQuality)}
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
                                                    <Label htmlFor="quick-auto-archive">자동 소장</Label>
                                                    <p className="text-xs text-muted-foreground">
                                                        이 스케줄로 녹화된 동영상을 자동으로 소장합니다.
                                                    </p>
                                                </div>
                                                <Switch
                                                    id="quick-auto-archive"
                                                    checked={autoArchive}
                                                    onCheckedChange={setAutoArchive}
                                                />
                                            </div>
                                            </div>
                                        </motion.div>
                                    )}
                                    </AnimatePresence>
                                </TabsContent>
                            </Tabs>
                            </div>
                            </motion.div>
                            </motion.div>
                        )}
                        </AnimatePresence>
                    </div>

                    <DialogFooter>
                        <DialogClose asChild>
                            <Button variant="outline" type="button">취소</Button>
                        </DialogClose>
                        <Button type="submit" disabled={isSubmitting || !isResolved}>
                            {isSubmitting ? (
                                <>
                                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                                    처리중...
                                </>
                            ) : (
                                "생성"
                            )}
                        </Button>
                    </DialogFooter>
                </form>
            </DialogContent>
        </Dialog>
    );
}
