"use client";

import { TimelineChart } from "@/app/(app)/videos/[uuid]/timeline-chart";
import { VideoSidePanel } from "@/app/(app)/videos/[uuid]/video-side-panel";
import type {
  VideoChapterGetResponse,
  VideoViewerHistoryGetResponse,
} from "@/lib/api/models";

interface TimelinePanelProps {
  videoUuid: string;
  currentTimeMs: number;
  /** 재생 시간(초). 챕터 마지막 구간의 끝을 잡는 데 쓴다. */
  durationSec: number;
  /** 시청자 수 이력. 채팅 수 그래프 위에 겹쳐 그린다. */
  viewerHistory?: VideoViewerHistoryGetResponse[];
  /** 카테고리 변경 이력(챕터). 시간축 옆 세로 띠로 표시한다. */
  chapters?: VideoChapterGetResponse[];
  isLive: boolean;
  /** 닫기(X) 버튼을 누르면 호출한다. */
  onClose: () => void;
  /** 타임라인에서 구간을 누르면 해당 시점으로 이동시킨다. */
  onSeek: (offsetMillis: number) => void;
}

// 채팅 영역을 덮어 통합 타임라인(채팅 수·시청자 수·챕터)을 보여주는 패널
// (공통 껍데기 VideoSidePanel에 차트를 끼운다)
export function TimelinePanel({
  videoUuid,
  currentTimeMs,
  durationSec,
  viewerHistory,
  chapters,
  isLive,
  onClose,
  onSeek,
}: TimelinePanelProps) {
  return (
    <VideoSidePanel title="타임라인" onClose={onClose}>
      <TimelineChart
        videoUuid={videoUuid}
        isLive={isLive}
        currentTimeMs={currentTimeMs}
        durationSec={durationSec}
        viewerHistory={viewerHistory}
        chapters={chapters}
        embedded
        onSeek={onSeek}
      />
    </VideoSidePanel>
  );
}
