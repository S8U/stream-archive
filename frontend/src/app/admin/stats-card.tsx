"use client";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { HardDrive, Clock, Tv, Video } from "lucide-react";
import { useGetAdminDashboardStats } from "@/lib/api/endpoints/admin-dashboard/admin-dashboard";
import { Skeleton } from "@/components/ui/skeleton";

interface StatCardProps {
    title: string;
    value: string;
    description?: string;
    icon: React.ReactNode;
    isLoading?: boolean;
}

function StatCard({ title, value, icon, isLoading }: StatCardProps) {
    return (
        <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">{title}</CardTitle>
                {icon}
            </CardHeader>
            <CardContent>
                {isLoading ? (
                    <Skeleton className="h-8 w-24" />
                ) : (
                    <div className="text-2xl font-bold">{value}</div>
                )}
            </CardContent>
        </Card>
    );
}

// 시간 포맷 함수 (초 -> 일/시간)
function formatDuration(seconds: number): string {
    const hours = seconds / 3600;
    if (hours >= 24) {
        const days = Math.floor(hours / 24);
        const remainingHours = Math.floor(hours % 24);
        return `${days}일 ${remainingHours}시간`;
    }
    return `${Math.floor(hours)}시간`;
}

// 스토리지 크기 포맷 함수 (바이트 -> TB/GB)
function formatStorage(bytes: number): string {
    const tb = bytes / (1024 * 1024 * 1024 * 1024);
    if (tb >= 1) {
        return `${tb.toFixed(2)} TB`;
    }
    const gb = bytes / (1024 * 1024 * 1024);
    if (gb >= 1) {
        return `${gb.toFixed(1)} GB`;
    }
    const mb = bytes / (1024 * 1024);
    return `${mb.toFixed(0)} MB`;
}

export function StatsCard() {
    const { data, isLoading } = useGetAdminDashboardStats();

    return (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
            <StatCard
                title="총 채널 수"
                value={data?.totalChannels?.toLocaleString() ?? "0"}
                icon={<Tv className="h-4 w-4 text-muted-foreground" />}
                isLoading={isLoading}
            />
            <StatCard
                title="총 동영상 수"
                value={data?.totalVideos?.toLocaleString() ?? "0"}
                icon={<Video className="h-4 w-4 text-muted-foreground" />}
                isLoading={isLoading}
            />
            <StatCard
                title="총 녹화 시간"
                value={formatDuration(data?.totalDuration ?? 0)}
                icon={<Clock className="h-4 w-4 text-muted-foreground" />}
                isLoading={isLoading}
            />
            <StatCard
                title="스토리지 사용량"
                value={formatStorage(data?.totalStorage ?? 0)}
                icon={<HardDrive className="h-4 w-4 text-muted-foreground" />}
                isLoading={isLoading}
            />
        </div>
    );
}
