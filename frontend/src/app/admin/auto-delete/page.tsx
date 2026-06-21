"use client";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Switch } from "@/components/ui/switch";
import { Card, CardContent } from "@/components/ui/card";
import {
    Dialog,
    DialogClose,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from "@/components/ui/dialog";
import { Separator } from "@/components/ui/separator";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { AdminBadge } from "@/components/common/admin-badge";
import { CustomPagination } from "@/components/common/custom-pagination";
import { AutoDeleteChannelPolicyDialog } from "@/components/admin/auto-delete-channel-policy-dialog";
import { cn } from "@/lib/utils";
import { Loader2, Play, ShieldCheck, TriangleAlert, Edit, Clock } from "lucide-react";
import { useCallback, useEffect, useRef, useState } from "react";
import Link from "next/link";
import { useQueryState, parseAsInteger, parseAsStringLiteral } from "nuqs";
import { useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import {
    useGetAdminAutoDeleteGlobalPolicy,
    useUpdateAdminAutoDeleteGlobalPolicy,
    useUpdateAdminAutoDeleteChannelPolicy,
    useDeleteAdminAutoDeleteChannelPolicy,
    useSearchAdminAutoDeleteChannelPolicies,
    useGetAdminAutoDeletePreviewSummary,
    useSearchAdminAutoDeletePreviews,
    useSearchAdminAutoDeleteHistories,
    useRunAdminAutoDelete,
} from "@/lib/api/endpoints/video-auto-delete-admin/video-auto-delete-admin";
import { useSearchAdminChannels } from "@/lib/api/endpoints/channel-admin/channel-admin";
import { MIN_DAYS, MAX_DAYS, DAY_PRESETS as PRESETS } from "./constants";

const AUTO_DELETE_QUERY_KEY = "/admin/videos/auto-delete";

const formatFileSize = (bytes: number) => {
    if (bytes === 0) return "0 B";
    const k = 1024;
    const sizes = ["B", "KB", "MB", "GB", "TB"];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + " " + sizes[i];
};

const formatDate = (dateString: string) => new Date(dateString).toLocaleDateString("ko-KR");

// 다음 자동 삭제 실행 시각 (매일 새벽 4시). 오늘 4시가 지났으면 내일 4시.
const nextRunLabel = () => {
    const now = new Date();
    const next = new Date(now);
    next.setHours(4, 0, 0, 0);
    if (now >= next) {
        next.setDate(next.getDate() + 1);
    }
    const mm = String(next.getMonth() + 1).padStart(2, "0");
    const dd = String(next.getDate()).padStart(2, "0");
    return `${mm}/${dd} 04:00`;
};

export default function AutoDeletePage() {
    const queryClient = useQueryClient();

    const { data: globalPolicy } = useGetAdminAutoDeleteGlobalPolicy();
    const globalEnabled = globalPolicy?.isEnabled ?? false;
    const globalDays = globalPolicy?.deleteAfterDays ?? null;

    // 자동 삭제 관련 쿼리를 모두 무효화한다.
    // 쿼리 키는 배열 요소 단위로 매칭되므로(["/.../policy"] 등 경로가 제각각),
    // 첫 요소 문자열이 자동 삭제 경로로 시작하는 쿼리를 predicate로 잡는다.
    const invalidateAll = useCallback(() => {
        queryClient.invalidateQueries({
            predicate: (query) => {
                const first = query.queryKey[0];
                return typeof first === "string" && first.startsWith(AUTO_DELETE_QUERY_KEY);
            },
        });
    }, [queryClient]);

    return (
        <div className="min-w-0">
            <h2 className="text-2xl font-bold">동영상 자동 삭제 관리</h2>
            <p className="text-muted-foreground">
                오래된 동영상을 자동으로 정리합니다.
            </p>

            {/* 히어로: 정책 + 요약/실행 */}
            <div className="mt-6 grid gap-4 xl:grid-cols-5">
                <GlobalPolicyCard onChanged={invalidateAll} />
                <RiskRunCard onChanged={invalidateAll} />
            </div>

            {/* 채널별 정책 / 미리보기 / 이력 */}
            <PolicyTabsSection globalEnabled={globalEnabled} globalDays={globalDays} onChanged={invalidateAll} />
        </div>
    );
}

// ── 전체 기본 정책 (스테퍼 + 프리셋) ─────────────────────────────
function GlobalPolicyCard({ onChanged }: { onChanged: () => void }) {
    const { data: policy } = useGetAdminAutoDeleteGlobalPolicy();
    const updateMutation = useUpdateAdminAutoDeleteGlobalPolicy();

    const [isEnabled, setIsEnabled] = useState(false);
    const [days, setDays] = useState("30");

    // 서버 값으로 폼을 채운다 (최초 1회만, 이후 재조회가 사용자 입력을 덮어쓰지 않게)
    const [loaded, setLoaded] = useState(false);
    useEffect(() => {
        if (policy && !loaded) {
            setIsEnabled(policy.isEnabled);
            setDays(policy.deleteAfterDays?.toString() ?? "30");
            setLoaded(true);
        }
    }, [policy, loaded]);

    const dayNum = Number(days);

    // 자동저장은 토글·프리셋·스텝·blur가 따로 호출돼 PUT이 동시에 나갈 수 있다.
    // 그러면 늦게 도착한 오래된 요청이 최종 저장값이 되어 화면과 서버가 어긋난다.
    // 그래서 한 번에 하나씩만 보내고, 저장 중에 들어온 값은 "마지막 것"만 모아 이어서 보낸다.
    const savingRef = useRef(false);
    const pendingRef = useRef<{ isEnabled: boolean; days: number } | null>(null);

    const flushSave = async () => {
        if (savingRef.current) return;

        savingRef.current = true;
        try {
            while (pendingRef.current) {
                const next = pendingRef.current;
                pendingRef.current = null;
                try {
                    await updateMutation.mutateAsync({ data: { isEnabled: next.isEnabled, deleteAfterDays: next.days } });
                    onChanged();
                    toast.success("전체 기본 정책이 저장되었습니다.", { id: "global-policy-save" });
                } catch {
                    toast.error("정책 저장에 실패했습니다.", { id: "global-policy-save" });
                }
            }
        } finally {
            savingRef.current = false;
        }
    };

    // 정책 저장을 예약한다 (실패해도 화면 상태는 유지하고 알림만)
    const save = (nextEnabled: boolean, nextDays: number) => {
        if (!Number.isInteger(nextDays) || nextDays < MIN_DAYS || nextDays > MAX_DAYS) return;
        pendingRef.current = { isEnabled: nextEnabled, days: nextDays };
        flushSave();
    };

    const toggleEnabled = (next: boolean) => {
        setIsEnabled(next);
        save(next, dayNum);
    };

    const setPreset = (d: number) => {
        setDays(d.toString());
        save(isEnabled, d);
    };

    const step = (delta: number) => {
        const next = Math.min(MAX_DAYS, Math.max(MIN_DAYS, (Number.isInteger(dayNum) ? dayNum : 30) + delta));
        setDays(next.toString());
        save(isEnabled, next);
    };

    // 일수 직접 입력은 다 친 뒤(blur) MIN_DAYS~MAX_DAYS로 보정해 화면·저장을 함께 맞춘다
    const commitDays = () => {
        const next = Math.min(MAX_DAYS, Math.max(MIN_DAYS, Number.isInteger(dayNum) ? dayNum : 30));
        setDays(next.toString());
        save(isEnabled, next);
    };

    return (
        <Card className="p-0 xl:col-span-2">
            <CardContent className="flex h-full flex-col gap-4 p-6">
                <div className="flex items-start justify-between gap-2">
                    <div>
                        <div className="flex items-center gap-2">
                            <span className="text-base font-semibold">전체 기본 정책</span>
                            {updateMutation.isPending && (
                                <Loader2 className="h-3.5 w-3.5 animate-spin text-muted-foreground" />
                            )}
                        </div>
                        <div className="mt-0.5 text-sm text-muted-foreground">
                            개별 설정이 없는 모든 채널에 적용
                        </div>
                    </div>
                    <Switch checked={isEnabled} onCheckedChange={toggleEnabled} />
                </div>

                <div className={cn("flex flex-1 flex-col gap-2 transition-opacity", isEnabled ? "opacity-100" : "pointer-events-none opacity-40")}>
                    <div className="flex flex-col gap-2">
                        <div className="text-sm text-muted-foreground">보관 기간</div>

                        <div className="flex items-center gap-2">
                            <Button variant="outline" size="icon" className="size-10 text-lg" onClick={() => step(-1)} disabled={!isEnabled}>−</Button>
                            <div className="flex items-baseline justify-center gap-1.5">
                                <Input
                                    type="number"
                                    min={1}
                                    max={MAX_DAYS}
                                    className="no-spinner h-11 w-28 text-center !text-2xl font-bold"
                                    value={days}
                                    onChange={(e) => setDays(e.target.value)}
                                    onBlur={commitDays}
                                    disabled={!isEnabled}
                                />
                                <span className="text-sm text-muted-foreground">일</span>
                            </div>
                            <Button variant="outline" size="icon" className="size-10 text-lg" onClick={() => step(1)} disabled={!isEnabled}>+</Button>
                        </div>
                    </div>

                    <div className="flex gap-2">
                        {PRESETS.map((d) => (
                            <Button
                                key={d}
                                variant={dayNum === d ? "default" : "outline"}
                                className="flex-1"
                                onClick={() => setPreset(d)}
                                disabled={!isEnabled}
                            >
                                {d}일
                            </Button>
                        ))}
                    </div>
                </div>
            </CardContent>
        </Card>
    );
}

// ── 다음 삭제 요약 + 즉시 실행 위험존 ─────────────────────────────
function RiskRunCard({ onChanged }: { onChanged: () => void }) {
    const { data: summary } = useGetAdminAutoDeletePreviewSummary();
    const runMutation = useRunAdminAutoDelete();
    const [dialogOpen, setDialogOpen] = useState(false);

    const count = summary?.targetCount ?? 0;

    const handleConfirm = async () => {
        try {
            const result = await runMutation.mutateAsync();
            toast.success(`${result.deletedCount}개 동영상을 삭제했습니다.`);
            setDialogOpen(false);
            onChanged();
        } catch {
            toast.error("자동 삭제 실행에 실패했습니다.");
        }
    };

    return (
        <Card className="overflow-hidden p-0 xl:col-span-3">
            <div className="flex h-full flex-col lg:flex-row">
                {/* 요약 수치 */}
                <div className="flex flex-1 flex-col justify-between gap-4 p-6">
                    <div className="flex items-center gap-2 text-sm font-medium text-muted-foreground">
                        <Clock className="h-4 w-4" />
                        다음 삭제 시간 <span className="font-semibold text-foreground tabular-nums">{nextRunLabel()}</span>
                    </div>

                    <div className="flex items-end gap-4">
                        <div className="flex flex-col gap-2">
                            <div className="text-sm text-muted-foreground">삭제될 동영상</div>
                            <div className="flex items-baseline gap-1">
                                <span className={cn("text-4xl font-bold leading-none tabular-nums", count > 0 ? "text-foreground" : "text-muted-foreground")}>
                                    {count}
                                </span>
                                <span className="text-sm text-muted-foreground">개</span>
                            </div>
                        </div>
                        <Separator orientation="vertical" className="h-11" />
                        <div className="flex flex-col gap-2">
                            <div className="text-sm text-muted-foreground">확보될 용량</div>
                            <div className={cn("whitespace-nowrap text-4xl font-bold leading-none tabular-nums", count > 0 ? "text-foreground" : "text-muted-foreground")}>
                                {formatFileSize(summary?.totalFileSize ?? 0)}
                            </div>
                        </div>
                    </div>

                    <div className="flex items-center gap-2 self-start rounded-md bg-emerald-500/10 px-3 py-2 text-sm text-muted-foreground">
                        <ShieldCheck className="h-4 w-4 shrink-0 text-emerald-500" />
                        소장한 동영상은 삭제되지 않습니다.
                    </div>
                </div>

                {/* 위험존 */}
                <div className="flex w-full shrink-0 flex-col gap-2 border-t bg-destructive/5 p-6 lg:w-72 lg:border-l lg:border-t-0">
                    <div className="flex items-center gap-2 text-sm font-semibold text-destructive">
                        <TriangleAlert className="h-4 w-4" />
                        즉시 실행
                    </div>
                    <p className="text-sm leading-relaxed text-muted-foreground">
                        스케줄을 기다리지 않고 지금 바로 정리합니다.{" "}
                        <span className="font-semibold text-destructive">삭제된 파일은 복구할 수 없습니다.</span>
                    </p>

                    <Button
                        variant="outline"
                        className="mt-auto border-destructive text-destructive hover:bg-destructive hover:text-white"
                        onClick={() => setDialogOpen(true)}
                        disabled={count === 0}
                    >
                        <Play />
                        지금 삭제 실행
                    </Button>
                </div>
            </div>

            <RunConfirmDialog
                open={dialogOpen}
                onOpenChange={setDialogOpen}
                count={count}
                totalFileSize={summary?.totalFileSize ?? 0}
                isRunning={runMutation.isPending}
                onConfirm={handleConfirm}
            />
        </Card>
    );
}

// 즉시 삭제 실행 확인 모달
function RunConfirmDialog({
    open,
    onOpenChange,
    count,
    totalFileSize,
    isRunning,
    onConfirm,
}: {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    count: number;
    totalFileSize: number;
    isRunning: boolean;
    onConfirm: () => void;
}) {
    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent>
                <DialogHeader>
                    <DialogTitle className="text-destructive">동영상 즉시 삭제</DialogTitle>
                    <DialogDescription>
                        스케줄을 기다리지 않고 지금 바로 정리합니다.{" "}
                        <span className="font-semibold text-destructive">삭제된 파일은 복구할 수 없습니다.</span>
                    </DialogDescription>
                </DialogHeader>

                <div className="flex items-end gap-5 py-2">
                    <div className="flex flex-col gap-1">
                        <div className="text-sm text-muted-foreground">삭제될 동영상</div>
                        <div className="flex items-baseline gap-1">
                            <span className="text-lg font-bold tabular-nums">{count}</span>
                            <span className="text-sm text-muted-foreground">개</span>
                        </div>
                    </div>
                    <Separator orientation="vertical" className="h-9" />
                    <div className="flex flex-col gap-1">
                        <div className="text-sm text-muted-foreground">확보될 용량</div>
                        <div className="text-lg font-bold tabular-nums">{formatFileSize(totalFileSize)}</div>
                    </div>
                </div>

                <DialogFooter>
                    <DialogClose asChild>
                        <Button variant="outline">취소</Button>
                    </DialogClose>
                    <Button variant="destructive" onClick={onConfirm} disabled={isRunning}>
                        {isRunning ? <Loader2 className="animate-spin" /> : null}
                        영구 삭제
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}

// ── 채널별 정책 ─────────────────────────────
function ChannelPolicyTab({
    globalEnabled,
    globalDays,
    onChanged,
    page,
    onPageChange,
}: {
    globalEnabled: boolean;
    globalDays: number | null;
    onChanged: () => void;
    page: number;
    onPageChange: (page: number) => void;
}) {
    const size = 10;

    // 현재 페이지의 채널만 받는다. 정책은 보통 채널 수보다 적으므로 전체를 한 번에 받아 매핑한다.
    const { data, isLoading } = useSearchAdminChannels({
        request: {},
        pageable: { page: page - 1, size },
    });

    const { data: channelPolicies } = useSearchAdminAutoDeleteChannelPolicies();
    const updateMutation = useUpdateAdminAutoDeleteChannelPolicy();
    const deleteMutation = useDeleteAdminAutoDeleteChannelPolicy();

    const [dialogOpen, setDialogOpen] = useState(false);
    const [selectedChannel, setSelectedChannel] = useState<{ id: number; name: string } | null>(null);

    const policyByChannelId = new Map((channelPolicies ?? []).map((p) => [p.channelId, p]));
    const channels = data?.content ?? [];
    const selectedPolicy = selectedChannel ? policyByChannelId.get(selectedChannel.id) : undefined;

    const handleSubmit = async (value: { isEnabled: boolean; deleteAfterDays: number }) => {
        if (!selectedChannel) return;
        try {
            await updateMutation.mutateAsync({ channelId: selectedChannel.id, data: value });
            toast.success(`"${selectedChannel.name}" 채널 정책이 저장되었습니다.`);
            onChanged();
            setDialogOpen(false);
        } catch {
            toast.error("채널 정책 저장에 실패했습니다.");
        }
    };

    // "전체 기본값 따름"은 채널별 정책을 지워서 전체 기본 정책을 따르게 한다.
    const handleResetToGlobal = async () => {
        if (!selectedChannel) return;
        try {
            await deleteMutation.mutateAsync({ channelId: selectedChannel.id });
            toast.success(`"${selectedChannel.name}" 채널이 전체 기본값을 따릅니다.`);
            onChanged();
            setDialogOpen(false);
        } catch {
            toast.error("채널 정책 변경에 실패했습니다.");
        }
    };

    return (
        <div>
            <div className="w-full overflow-x-auto rounded-lg border">
                <Table className="w-full">
                    <TableHeader className="bg-muted">
                        <TableRow>
                            <TableHead className="border-r font-medium">채널 정보</TableHead>
                            <TableHead className="border-r font-medium w-[120px] text-center">정책</TableHead>
                            <TableHead className="border-r font-medium w-[120px] text-center">자동 삭제</TableHead>
                            <TableHead className="border-r font-medium w-[110px] text-center">보관 기간</TableHead>
                            <TableHead className="font-medium w-[90px] text-center">작업</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {isLoading ? (
                            <TableRow>
                                <TableCell colSpan={5} className="py-8 text-center">
                                    <Loader2 className="mx-auto h-6 w-6 animate-spin" />
                                </TableCell>
                            </TableRow>
                        ) : channels.length === 0 ? (
                            <TableRow>
                                <TableCell colSpan={5} className="py-8 text-center text-muted-foreground">
                                    등록된 채널이 없습니다.
                                </TableCell>
                            </TableRow>
                        ) : (
                            channels.map((channel) => {
                                const policy = policyByChannelId.get(channel.id);
                                // 적용 정책: 채널 정책 있으면 그 값, 없으면 전체 기본 정책
                                const effectiveEnabled = policy ? policy.isEnabled : globalEnabled;
                                const effectiveDays = policy ? policy.deleteAfterDays : globalDays;
                                return (
                                    <TableRow key={channel.id}>
                                        <TableCell className="border-r">
                                            <div className="flex items-center gap-2">
                                                <Avatar className="h-8 w-8">
                                                    <AvatarImage src={channel.profileUrl} />
                                                    <AvatarFallback>{channel.name[0]?.toUpperCase()}</AvatarFallback>
                                                </Avatar>
                                                <span className="font-medium">{channel.name}</span>
                                            </div>
                                        </TableCell>
                                        <TableCell className="border-r text-center">
                                            <AdminBadge tone={policy ? "info" : "neutral"}>
                                                {policy ? "개별 설정" : "기본값 따름"}
                                            </AdminBadge>
                                        </TableCell>
                                        <TableCell className="border-r text-center">
                                            <AdminBadge tone={effectiveEnabled ? "success" : "neutral"}>
                                                {effectiveEnabled ? "켜짐" : "삭제 안 함"}
                                            </AdminBadge>
                                        </TableCell>
                                        <TableCell className="border-r text-center tabular-nums">
                                            {effectiveEnabled && effectiveDays != null ? (
                                                `${effectiveDays}일`
                                            ) : (
                                                <span className="text-muted-foreground">-</span>
                                            )}
                                        </TableCell>
                                        <TableCell className="text-center">
                                            <Button
                                                variant="secondary"
                                                size="icon"
                                                onClick={() => {
                                                    setSelectedChannel({ id: channel.id, name: channel.name });
                                                    setDialogOpen(true);
                                                }}
                                            >
                                                <Edit />
                                            </Button>
                                        </TableCell>
                                    </TableRow>
                                );
                            })
                        )}
                    </TableBody>
                </Table>
            </div>

            {data && (
                <CustomPagination page={page - 1} totalPages={data.totalPages || 0} onPageChange={(p) => onPageChange(p + 1)} />
            )}

            <AutoDeleteChannelPolicyDialog
                open={dialogOpen}
                onOpenChange={setDialogOpen}
                channelName={selectedChannel?.name ?? ""}
                globalDays={globalDays}
                initialValue={
                    selectedPolicy
                        ? { isEnabled: selectedPolicy.isEnabled, deleteAfterDays: selectedPolicy.deleteAfterDays }
                        : null
                }
                onSubmit={handleSubmit}
                onResetToGlobal={handleResetToGlobal}
                isSubmitting={updateMutation.isPending || deleteMutation.isPending}
            />
        </div>
    );
}

// ── 채널별 정책 / 미리보기 / 이력 탭 ─────────────────────────────
const TAB_VALUES = ["channels", "preview", "history"] as const;

function PolicyTabsSection({
    globalEnabled,
    globalDays,
    onChanged,
}: {
    globalEnabled: boolean;
    globalDays: number | null;
    onChanged: () => void;
}) {
    // 탭 배지 개수만 필요하므로 한 건만 받아 전체 개수를 읽는다.
    const { data: channelsData } = useSearchAdminChannels({ request: {}, pageable: { page: 0, size: 1 } });
    const { data: previewSummary } = useGetAdminAutoDeletePreviewSummary();
    const { data: historyData } = useSearchAdminAutoDeleteHistories({ pageable: { page: 0, size: 1 } });

    // 탭과 페이지를 URL에 함께 둔다. 페이지 키는 탭 공용으로 하나만 둬서 URL에 한 번만 남게 한다.
    // (탭마다 페이지 키를 따로 두면 비활성 탭의 페이지까지 URL에 박혀 남는다)
    const [tab, setTab] = useQueryState("tab", parseAsStringLiteral(TAB_VALUES).withDefault("channels"));
    const [page, setPage] = useQueryState("page", parseAsInteger.withDefault(1));

    // 탭을 바꾸면 페이지를 1로 되돌린다 (이전 탭의 페이지 번호가 새 탭에 새지 않게)
    const changeTab = (next: (typeof TAB_VALUES)[number]) => {
        setTab(next);
        setPage(1);
    };

    const channelCount = channelsData?.totalElements ?? 0;
    const previewCount = previewSummary?.targetCount ?? 0;
    const historyTotal = historyData?.totalElements ?? 0;

    return (
        <Tabs value={tab} onValueChange={(v) => changeTab(v as (typeof TAB_VALUES)[number])} className="mt-6 gap-2">
            <TabsList>
                <TabsTrigger value="channels">
                    채널별 정책
                    <TabCount tone="muted">{channelCount}</TabCount>
                </TabsTrigger>
                <TabsTrigger value="preview">
                    삭제 미리보기
                    <TabCount tone="muted">{previewCount}</TabCount>
                </TabsTrigger>
                <TabsTrigger value="history">
                    삭제 이력
                    <TabCount tone="muted">{historyTotal}</TabCount>
                </TabsTrigger>
            </TabsList>

            <TabsContent value="channels">
                <ChannelPolicyTab
                    globalEnabled={globalEnabled}
                    globalDays={globalDays}
                    onChanged={onChanged}
                    page={page}
                    onPageChange={setPage}
                />
            </TabsContent>
            <TabsContent value="preview">
                <PreviewTab page={page} onPageChange={setPage} />
            </TabsContent>
            <TabsContent value="history">
                <HistoryTab page={page} onPageChange={setPage} />
            </TabsContent>
        </Tabs>
    );
}

function TabCount({ tone, children }: { tone: "danger" | "muted"; children: React.ReactNode }) {
    return (
        <span
            className={cn(
                "rounded-full px-1.5 py-0.5 text-xs tabular-nums",
                tone === "danger" ? "bg-destructive/10 text-destructive" : "bg-muted text-muted-foreground"
            )}
        >
            {children}
        </span>
    );
}

function PreviewTab({ page, onPageChange }: { page: number; onPageChange: (page: number) => void }) {
    const size = 10;

    const { data: channelsData } = useSearchAdminChannels({ request: {}, pageable: { page: 0, size: 200 } });

    const { data, isLoading, error } = useSearchAdminAutoDeletePreviews({
        pageable: { page: page - 1, size },
    });

    const channels = channelsData?.content ?? [];
    const profileByChannelId = new Map(channels.map((c) => [c.id, c.profileUrl]));

    return (
        <div>
            <PreviewTable
                data={data}
                isLoading={isLoading}
                isError={!!error}
                profileByChannelId={profileByChannelId}
            />

            {data && (
                <CustomPagination page={page - 1} totalPages={data.totalPages || 0} onPageChange={(p) => onPageChange(p + 1)} />
            )}
        </div>
    );
}

// 기준 초과 일수에 따른 경과 배지 톤 (초과가 클수록 위험)
function overTone(overDays: number): "danger" | "warning" {
    return overDays > 30 ? "danger" : "warning";
}

function PreviewTable({
    data,
    isLoading,
    isError,
    profileByChannelId,
}: {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    data: any;
    isLoading: boolean;
    isError: boolean;
    profileByChannelId: Map<number, string>;
}) {
    return (
        <div className="w-full overflow-x-auto rounded-lg border">
            <Table className="w-full">
                <TableHeader className="bg-muted">
                    <TableRow>
                        <TableHead className="border-r font-medium w-[60px] text-center">ID</TableHead>
                        <TableHead className="border-r font-medium">채널 정보</TableHead>
                        <TableHead className="border-r font-medium">동영상 정보</TableHead>
                        <TableHead className="border-r font-medium w-[110px] text-center">용량</TableHead>
                        <TableHead className="border-r font-medium w-[120px] text-center">생성일</TableHead>
                        <TableHead className="font-medium w-[110px] text-center">경과</TableHead>
                    </TableRow>
                </TableHeader>
                <TableBody>
                    {isLoading ? (
                        <TableRow><TableCell colSpan={6} className="py-8 text-center"><Loader2 className="mx-auto h-6 w-6 animate-spin" /></TableCell></TableRow>
                    ) : isError ? (
                        <TableRow><TableCell colSpan={6} className="py-8 text-center text-destructive">데이터를 불러오는 중 오류가 발생했습니다.</TableCell></TableRow>
                    ) : !data?.content || data.content.length === 0 ? (
                        <TableRow><TableCell colSpan={6} className="py-10 text-center text-muted-foreground">현재 정책으로 삭제될 동영상이 없습니다.</TableCell></TableRow>
                    ) : (
                        // eslint-disable-next-line @typescript-eslint/no-explicit-any
                        data.content.map((video: any) => (
                            <TableRow key={video.id}>
                                <TableCell className="border-r text-center">{video.id}</TableCell>
                                <TableCell className="border-r">
                                    <div className="flex items-center gap-2">
                                        <Avatar className="h-8 w-8">
                                            <AvatarImage src={profileByChannelId.get(video.channelId)} />
                                            <AvatarFallback>{video.channelName[0]?.toUpperCase()}</AvatarFallback>
                                        </Avatar>
                                        <span className="font-medium">{video.channelName}</span>
                                    </div>
                                </TableCell>
                                <TableCell className="border-r">
                                    <div className="flex items-center gap-3">
                                        <div className="relative w-16 h-9 bg-muted rounded overflow-hidden flex-shrink-0">
                                            {video.thumbnailUrl ? (
                                                // eslint-disable-next-line @next/next/no-img-element
                                                <img src={video.thumbnailUrl} alt={video.title} className="w-full h-full object-cover" />
                                            ) : (
                                                <div className="w-full h-full flex items-center justify-center text-xs text-muted-foreground">No Img</div>
                                            )}
                                        </div>
                                        <Link
                                            href={`/videos/${video.uuid}`}
                                            target="_blank"
                                            rel="noopener noreferrer"
                                            className="hover:underline truncate max-w-[300px]"
                                        >
                                            <span className="truncate">{video.title}</span>
                                        </Link>
                                    </div>
                                </TableCell>
                                <TableCell className="border-r text-center tabular-nums">{formatFileSize(video.fileSize)}</TableCell>
                                <TableCell className="border-r text-center tabular-nums">{formatDate(video.createdAt)}</TableCell>
                                <TableCell className="text-center">
                                    <AdminBadge tone={overTone(video.overDays)}>기준 +{video.overDays}일</AdminBadge>
                                </TableCell>
                            </TableRow>
                        ))
                    )}
                </TableBody>
            </Table>
        </div>
    );
}

function HistoryTab({ page, onPageChange }: { page: number; onPageChange: (page: number) => void }) {
    const size = 10;

    const { data: channelsData } = useSearchAdminChannels({ request: {}, pageable: { page: 0, size: 200 } });

    const { data, isLoading, error } = useSearchAdminAutoDeleteHistories({ pageable: { page: page - 1, size } });

    const channels = channelsData?.content ?? [];
    const profileByChannelId = new Map(channels.map((c) => [c.id, c.profileUrl]));

    return (
        <div>
            <div className="w-full overflow-x-auto rounded-lg border">
                <Table className="w-full">
                    <TableHeader className="bg-muted">
                        <TableRow>
                            <TableHead className="border-r font-medium w-[60px] text-center">ID</TableHead>
                            <TableHead className="border-r font-medium">채널 정보</TableHead>
                            <TableHead className="border-r font-medium">제목</TableHead>
                            <TableHead className="border-r font-medium w-[110px] text-center">용량</TableHead>
                            <TableHead className="border-r font-medium w-[120px] text-center">생성일</TableHead>
                            <TableHead className="font-medium w-[120px] text-center">삭제일</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {isLoading ? (
                            <TableRow><TableCell colSpan={6} className="py-8 text-center"><Loader2 className="mx-auto h-6 w-6 animate-spin" /></TableCell></TableRow>
                        ) : error ? (
                            <TableRow><TableCell colSpan={6} className="py-8 text-center text-destructive">데이터를 불러오는 중 오류가 발생했습니다.</TableCell></TableRow>
                        ) : !data?.content || data.content.length === 0 ? (
                            <TableRow><TableCell colSpan={6} className="py-10 text-center text-muted-foreground">자동 삭제 이력이 없습니다.</TableCell></TableRow>
                        ) : (
                            data.content.map((history) => (
                                <TableRow key={history.id}>
                                    <TableCell className="border-r text-center">{history.videoId}</TableCell>
                                    <TableCell className="border-r">
                                        <div className="flex items-center gap-2">
                                            <Avatar className="h-8 w-8">
                                                <AvatarImage src={profileByChannelId.get(history.channelId)} />
                                                <AvatarFallback>{history.channelName[0]?.toUpperCase()}</AvatarFallback>
                                            </Avatar>
                                            <span className="font-medium">{history.channelName}</span>
                                        </div>
                                    </TableCell>
                                    <TableCell className="border-r">
                                        <span className="block max-w-[400px] truncate">{history.title}</span>
                                    </TableCell>
                                    <TableCell className="border-r text-center tabular-nums">{formatFileSize(history.fileSize)}</TableCell>
                                    <TableCell className="border-r text-center tabular-nums">{formatDate(history.videoCreatedAt)}</TableCell>
                                    <TableCell className="text-center tabular-nums">{formatDate(history.deletedAt)}</TableCell>
                                </TableRow>
                            ))
                        )}
                    </TableBody>
                </Table>
            </div>

            {data && (
                <CustomPagination page={page - 1} totalPages={data.totalPages || 0} onPageChange={(p) => onPageChange(p + 1)} />
            )}
        </div>
    );
}
