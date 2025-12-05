'use client';

import { useMemo } from 'react';
import {
    LineChart,
    Line,
    BarChart,
    Bar,
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip,
    Legend,
    ResponsiveContainer,
    ReferenceLine,
    Area,
    ComposedChart
} from 'recharts';
import type { ChatBucket, RSIData, HighlightSegment } from '@/lib/utils/highlight-utils';

interface HighlightChartsProps {
    chatBuckets: ChatBucket[];
    rsiData: RSIData[];
    highlights: HighlightSegment[];
    currentTimeMs: number;
    onSeek?: (timeMs: number) => void;
}

// 시간을 MM:SS 형식으로 변환
function formatTime(ms: number): string {
    const seconds = Math.floor(ms / 1000);
    const minutes = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${minutes}:${secs.toString().padStart(2, '0')}`;
}

export function HighlightCharts({
    chatBuckets,
    rsiData,
    highlights,
    currentTimeMs,
    onSeek
}: HighlightChartsProps) {
    // 채팅 그래프 데이터 준비
    const chatChartData = useMemo(() => {
        return chatBuckets.map(bucket => ({
            time: formatTime(bucket.startTimeMs),
            timeMs: bucket.startTimeMs,
            count: bucket.count
        }));
    }, [chatBuckets]);

    // RSI 그래프 데이터 준비
    const rsiChartData = useMemo(() => {
        return rsiData.map(data => ({
            time: formatTime(data.timeMs),
            timeMs: data.timeMs,
            rsi: data.rsi
        }));
    }, [rsiData]);

    // 클릭 핸들러
    const handleChartClick = (data: any) => {
        if (data?.activePayload?.[0]?.payload?.timeMs !== undefined && onSeek) {
            onSeek(data.activePayload[0].payload.timeMs);
        }
    };

    return (
        <div className="space-y-6 p-4 bg-muted/30 rounded-lg">
            <div>
                <h3 className="text-sm font-semibold mb-3">채팅 개수 (10초 단위)</h3>
                <ResponsiveContainer width="100%" height={200}>
                    <BarChart
                        data={chatChartData}
                        onClick={handleChartClick}
                        className="cursor-pointer"
                    >
                        <CartesianGrid strokeDasharray="3 3" />
                        <XAxis
                            dataKey="time"
                            interval="preserveStartEnd"
                            minTickGap={50}
                        />
                        <YAxis />
                        <Tooltip
                            labelFormatter={(label) => `시간: ${label}`}
                            formatter={(value: number) => [`${value}개`, '채팅 개수']}
                        />
                        <Bar dataKey="count" fill="#8884d8" />

                        {/* 현재 재생 위치 표시 */}
                        <ReferenceLine
                            x={formatTime(Math.floor(currentTimeMs / 10000) * 10000)}
                            stroke="red"
                            strokeWidth={2}
                            label={{ value: '현재', position: 'top' }}
                        />
                    </BarChart>
                </ResponsiveContainer>
            </div>

            <div>
                <h3 className="text-sm font-semibold mb-3">
                    RSI (Relative Strength Index)
                    <span className="text-xs text-muted-foreground ml-2">
                        피크: 60 이상 | 구간: 45~45
                    </span>
                </h3>
                <ResponsiveContainer width="100%" height={200}>
                    <ComposedChart
                        data={rsiChartData}
                        onClick={handleChartClick}
                        className="cursor-pointer"
                    >
                        <CartesianGrid strokeDasharray="3 3" />
                        <XAxis
                            dataKey="time"
                            interval="preserveStartEnd"
                            minTickGap={50}
                        />
                        <YAxis domain={[0, 100]} />
                        <Tooltip
                            labelFormatter={(label) => `시간: ${label}`}
                            formatter={(value: number) => [value.toFixed(2), 'RSI']}
                        />

                        {/* RSI 선 */}
                        <Line
                            type="monotone"
                            dataKey="rsi"
                            stroke="#82ca9d"
                            strokeWidth={2}
                            dot={false}
                        />

                        {/* 하이라이트 구간 표시 */}
                        {highlights.map((highlight, index) => (
                            <ReferenceLine
                                key={index}
                                segment={[
                                    { x: formatTime(highlight.startTimeMs), y: 0 },
                                    { x: formatTime(highlight.endTimeMs), y: 100 }
                                ]}
                                stroke="rgba(255, 165, 0, 0.5)"
                                strokeWidth={20}
                                ifOverflow="extendDomain"
                            />
                        ))}

                        {/* 임계값 선 */}
                        <ReferenceLine y={60} stroke="orange" strokeDasharray="3 3" label="피크(60)" />
                        <ReferenceLine y={45} stroke="blue" strokeDasharray="3 3" label="구간(45)" />

                        {/* 현재 재생 위치 */}
                        <ReferenceLine
                            x={formatTime(Math.floor(currentTimeMs / 10000) * 10000)}
                            stroke="red"
                            strokeWidth={2}
                            label={{ value: '현재', position: 'top' }}
                        />
                    </ComposedChart>
                </ResponsiveContainer>
            </div>

            {/* 하이라이트 구간 목록 */}
            {highlights.length > 0 && (
                <div>
                    <h3 className="text-sm font-semibold mb-3">하이라이트 구간 ({highlights.length}개)</h3>
                    <div className="space-y-2">
                        {highlights.map((highlight, index) => (
                            <button
                                key={index}
                                onClick={() => onSeek?.(highlight.startTimeMs)}
                                className="w-full text-left p-3 bg-background rounded-md hover:bg-accent transition-colors"
                            >
                                <div className="flex items-center justify-between">
                                    <div className="flex items-center gap-2">
                                        <span className="text-xs font-mono bg-primary/10 px-2 py-1 rounded">
                                            #{index + 1}
                                        </span>
                                        <span className="text-sm">
                                            {formatTime(highlight.startTimeMs)} - {formatTime(highlight.endTimeMs)}
                                        </span>
                                    </div>
                                    <div className="flex items-center gap-2">
                                        <span className="text-xs text-muted-foreground">
                                            RSI: {highlight.rsiScore.toFixed(1)}
                                        </span>
                                        <span className="text-xs text-muted-foreground">
                                            ({Math.floor((highlight.endTimeMs - highlight.startTimeMs) / 1000)}초)
                                        </span>
                                    </div>
                                </div>
                            </button>
                        ))}
                    </div>
                </div>
            )}
        </div>
    );
}
