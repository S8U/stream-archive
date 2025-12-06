"use client";

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { ChartConfig, ChartContainer, ChartTooltip, ChartTooltipContent } from "@/components/ui/chart";
import { Area, AreaChart, CartesianGrid, XAxis, YAxis } from "recharts";
import { useGetAdminDashboardVideoHistories } from "@/lib/api/endpoints/admin-dashboard/admin-dashboard";
import { Skeleton } from "@/components/ui/skeleton";
import { useMemo } from "react";

const chartConfig = {
    videoCount: {
        label: "동영상 수",
        color: "var(--chart-1)",
    },
    storageUsage: {
        label: "스토리지(GB)",
        color: "var(--chart-2)",
    },
} satisfies ChartConfig;

// 바이트를 GB로 변환
function bytesToGB(bytes: number): number {
    return Math.round((bytes / (1024 * 1024 * 1024)) * 10) / 10;
}

export function VideoHistoryCard() {
    const { data, isLoading } = useGetAdminDashboardVideoHistories();

    // API 데이터를 차트 데이터로 변환
    const chartData = useMemo(() => {
        if (!data?.dailyStats) return [];
        return data.dailyStats.map((stat) => ({
            date: new Date(stat.date).toLocaleDateString("ko-KR", { month: "2-digit", day: "2-digit" }),
            videoCount: stat.videoCount,
            storageUsage: bytesToGB(stat.storageUsage),
        }));
    }, [data]);

    return (
        <Card>
            <CardHeader>
                <CardTitle>동영상 현황</CardTitle>
                <CardDescription>최근 30일간 동영상 현황</CardDescription>
            </CardHeader>
            <CardContent>
                {isLoading ? (
                    <Skeleton className="h-[300px] w-full" />
                ) : (
                    <ChartContainer config={chartConfig} className="h-[300px] w-full">
                        <AreaChart
                            accessibilityLayer
                            data={chartData}
                            margin={{ left: 0, right: 0, top: 10, bottom: 0 }}
                        >
                            <CartesianGrid vertical={false} />
                            <XAxis
                                dataKey="date"
                                tickLine={false}
                                axisLine={false}
                                tickMargin={8}
                                interval="preserveStartEnd"
                            />
                            <YAxis
                                yAxisId="left"
                                tickLine={false}
                                axisLine={false}
                                tickMargin={8}
                                width={35}
                            />
                            <YAxis
                                yAxisId="right"
                                orientation="right"
                                tickLine={false}
                                axisLine={false}
                                tickMargin={8}
                                width={40}
                            />
                            <ChartTooltip
                                cursor={false}
                                content={<ChartTooltipContent />}
                            />
                            <Area
                                yAxisId="left"
                                dataKey="videoCount"
                                type="monotone"
                                fill="var(--color-videoCount)"
                                fillOpacity={0.4}
                                stroke="var(--color-videoCount)"
                                strokeWidth={2}
                            />
                            <Area
                                yAxisId="right"
                                dataKey="storageUsage"
                                type="monotone"
                                fill="var(--color-storageUsage)"
                                fillOpacity={0.2}
                                stroke="var(--color-storageUsage)"
                                strokeWidth={2}
                            />
                        </AreaChart>
                    </ChartContainer>
                )}
            </CardContent>
        </Card>
    );
}
