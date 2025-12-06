"use client";

import { StatsCard } from "./stats-card";
import { VideoHistoryCard } from "./video-history-card";
import { VideoListCard } from "./video-list-card";

export default function AdminDashboardPage() {
    return (
        <div className="min-w-0 space-y-6">
            {/* 페이지 헤더 */}
            <div>
                <h2 className="text-2xl font-bold">대시보드</h2>
                <p className="text-muted-foreground">시스템 현황을 확인합니다.</p>
            </div>

            {/* 통계 카드 섹션 */}
            <StatsCard />

            {/* 차트 섹션 */}
            <VideoHistoryCard />

            {/* 최근 동영상 섹션 */}
            <VideoListCard />
        </div>
    );
}