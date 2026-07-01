import type { Metadata } from "next";
import { WatchHistoryList } from "./watch-history-list";

export const metadata: Metadata = {
    title: "시청 기록",
    description: "최근에 시청한 동영상을 확인하세요",
};

export default function WatchHistoryPage() {
    return (
        <div className="p-4 md:p-6">
            <WatchHistoryList />
        </div>
    );
}
