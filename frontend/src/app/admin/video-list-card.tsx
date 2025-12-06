"use client";

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { PlatformBadge } from "@/components/common/platform-badge";
import { Skeleton } from "@/components/ui/skeleton";
import { useSearchAdminRecords } from "@/lib/api/endpoints/admin-record/admin-record";
import Link from "next/link";

// 길이 포맷
function formatDuration(seconds: number): string {
    const hours = Math.floor(seconds / 3600);
    const mins = Math.floor((seconds % 3600) / 60);
    const secs = seconds % 60;

    if (hours > 0) {
        return `${hours}:${mins.toString().padStart(2, "0")}:${secs.toString().padStart(2, "0")}`;
    }
    return `${mins}:${secs.toString().padStart(2, "0")}`;
}

// 날짜 포맷
function formatDate(dateString: string): string {
    return new Date(dateString).toLocaleString("ko-KR");
}

export function VideoListCard() {
    const { data, isLoading } = useSearchAdminRecords({
        request: {},
        pageable: {
            page: 0,
            size: 10,
            sort: ["createdAt,desc"],
        },
    });

    const records = data?.content ?? [];

    return (
        <Card>
            <CardHeader>
                <CardTitle className="text-base">최근 동영상</CardTitle>
                <CardDescription>진행 중인 녹화 및 최근 생성된 동영상</CardDescription>
            </CardHeader>
            <CardContent>
                {isLoading ? (
                    <Skeleton className="h-[300px] w-full" />
                ) : (
                    <div className="rounded-lg border overflow-hidden overflow-x-auto">
                        <Table>
                            <TableHeader className="bg-muted">
                                <TableRow>
                                    <TableHead className="border-r font-semibold">채널 정보</TableHead>
                                    <TableHead className="border-r font-semibold w-[90px] text-center">플랫폼</TableHead>
                                    <TableHead className="border-r font-semibold">동영상 정보</TableHead>
                                    <TableHead className="border-r font-semibold w-[80px] text-center">상태</TableHead>
                                    <TableHead className="border-r font-semibold w-[160px] text-center">시작 시간</TableHead>
                                    <TableHead className="border-r font-semibold w-[160px] text-center">종료 시간</TableHead>
                                    <TableHead className="font-semibold w-[80px] text-center">길이</TableHead>
                                </TableRow>
                            </TableHeader>
                            <TableBody>
                                {records.length === 0 ? (
                                    <TableRow>
                                        <TableCell colSpan={7} className="text-center py-8 text-muted-foreground">
                                            최근 활동이 없습니다.
                                        </TableCell>
                                    </TableRow>
                                ) : (
                                    records.map((record) => (
                                        <TableRow key={record.id}>
                                            {/* 채널 정보 */}
                                            <TableCell className="border-r">
                                                <div className="flex items-center gap-2">
                                                    <Avatar className="h-8 w-8">
                                                        <AvatarImage src={record.channel?.profileUrl} />
                                                        <AvatarFallback>{record.channel?.name?.[0]?.toUpperCase()}</AvatarFallback>
                                                    </Avatar>
                                                    {record.channel?.uuid ? (
                                                        <Link
                                                            href={`/channels/${record.channel.uuid}`}
                                                            target="_blank"
                                                            rel="noopener noreferrer"
                                                            className="hover:underline"
                                                        >
                                                            {record.channel?.name}
                                                        </Link>
                                                    ) : (
                                                        <span>{record.channel?.name}</span>
                                                    )}
                                                </div>
                                            </TableCell>

                                            {/* 플랫폼 */}
                                            <TableCell className="border-r text-center">
                                                {record.platformType && <PlatformBadge platform={record.platformType} />}
                                            </TableCell>

                                            {/* 동영상 정보 (썸네일 + 제목) */}
                                            <TableCell className="border-r">
                                                <div className="flex items-center gap-3">
                                                    <div className="relative w-16 h-9 bg-muted rounded overflow-hidden flex-shrink-0">
                                                        {record.video?.thumbnailUrl ? (
                                                            <img src={record.video.thumbnailUrl} alt={record.video?.title} className="w-full h-full object-cover" />
                                                        ) : (
                                                            <div className="w-full h-full flex items-center justify-center text-xs text-muted-foreground">No Img</div>
                                                        )}
                                                    </div>
                                                    {record.video?.uuid ? (
                                                        <Link
                                                            href={`/videos/${record.video.uuid}`}
                                                            target="_blank"
                                                            rel="noopener noreferrer"
                                                            className="hover:underline truncate"
                                                        >
                                                            {record.video?.title}
                                                        </Link>
                                                    ) : (
                                                        <span className="truncate">{record.video?.title}</span>
                                                    )}
                                                </div>
                                            </TableCell>

                                            {/* 상태 */}
                                            <TableCell className="border-r text-center">
                                                {!record.isEnded && !record.isCancelled ? (
                                                    <Badge className="bg-red-100 text-red-700 hover:bg-red-100/80">녹화중</Badge>
                                                ) : record.isCancelled ? (
                                                    <Badge variant="destructive">취소</Badge>
                                                ) : (
                                                    <Badge variant="secondary">완료</Badge>
                                                )}
                                            </TableCell>

                                            {/* 시작 시간 */}
                                            <TableCell className="border-r text-center text-sm">
                                                {record.createdAt ? formatDate(record.createdAt) : "-"}
                                            </TableCell>

                                            {/* 종료 시간 */}
                                            <TableCell className="border-r text-center text-sm">
                                                {record.endedAt ? formatDate(record.endedAt) : "-"}
                                            </TableCell>

                                            {/* 길이 - Video의 duration 사용 */}
                                            <TableCell className="text-center text-sm">
                                                {record.video?.duration ? (
                                                    <span className="font-mono">{formatDuration(record.video.duration)}</span>
                                                ) : (
                                                    "-"
                                                )}
                                            </TableCell>
                                        </TableRow>
                                    ))
                                )}
                            </TableBody>
                        </Table>
                    </div>
                )}
            </CardContent>
        </Card>
    );
}
