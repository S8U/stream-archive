"use client";

import { useState, useEffect, useRef, useCallback, useMemo } from "react";
import { useSearchParams } from "next/navigation";
import { VideoPlayer } from "@/app/(app)/videos/[uuid]/video-player";
import { VideoInfo } from "@/components/video-info";
import { ChatHistory } from "@/app/(app)/videos/[uuid]/chat-history";
import { TimelinePanel } from "@/app/(app)/videos/[uuid]/timeline-panel";
import {
  useGetVideoByUuid,
  useGetVideoChapters,
  useGetVideoViewerHistory,
  useGetVideoWatchHistory,
  useSaveVideoWatchHistory,
} from "@/lib/api/endpoints/video/video";
import { useGetUserMe } from "@/lib/api/endpoints/user/user";
import { VideoGetResponse } from "@/lib/api/models";

interface VideoWatchViewProps {
  video: VideoGetResponse;
}

const LIVE_VIDEO_REFRESH_INTERVAL_MS = 10_000;

function isRecordingVideo(video?: VideoGetResponse): boolean {
  return !!video?.record && !video.record.isEnded && !video.record.isCancelled;
}

function parseTimeParam(value: string | null): number | null {
  if (!value) return null;
  const normalized = value.trim().toLowerCase();
  if (!normalized) return null;

  if (/^\d+$/.test(normalized)) {
    return Number(normalized);
  }

  const match = normalized.match(/^(?:(\d+)h)?(?:(\d+)m)?(?:(\d+)s?)?$/);
  if (!match || !match[0]) return null;

  const hours = Number(match[1] ?? 0);
  const minutes = Number(match[2] ?? 0);
  const seconds = Number(match[3] ?? 0);
  const total = hours * 3600 + minutes * 60 + seconds;

  return total > 0 ? total : null;
}

export function VideoWatchView({ video }: VideoWatchViewProps) {
  const searchParams = useSearchParams();
  const urlInitialPosition = useMemo(
    () => parseTimeParam(searchParams.get("t")),
    [searchParams],
  );
  const [currentTimeMs, setCurrentTimeMs] = useState(0);
  const [initialPosition, setInitialPosition] = useState<number | null>(
    () => urlInitialPosition,
  );
  const [seekRequest, setSeekRequest] = useState<{
    seconds: number;
    nonce: number;
  } | null>(null);
  const lastSavedPositionRef = useRef(0);
  const currentPositionRef = useRef(0);
  const { data: user } = useGetUserMe({
    query: {
      retry: false,
    },
  });
  const isAuthenticated = !!user;

  const { data: currentVideo = video } = useGetVideoByUuid(video.uuid, {
    query: {
      initialData: video,
      refetchInterval: (query) =>
        isRecordingVideo(query.state.data)
          ? LIVE_VIDEO_REFRESH_INTERVAL_MS
          : false,
      staleTime: isRecordingVideo(video) ? 0 : 60_000,
    },
  });

  // 라이브 여부: 녹화가 진행 중이면 라이브
  const isLive = isRecordingVideo(currentVideo);

  // 시청 기록 조회
  const { data: watchHistory } = useGetVideoWatchHistory(currentVideo.uuid, {
    query: {
      enabled: isAuthenticated,
      retry: false,
      staleTime: 0,
    },
  });

  // 시청 위치 저장 mutation
  const saveWatchHistory = useSaveVideoWatchHistory();

  // 시청 기록이 있으면 초기 위치 설정 (라이브는 이어보기 의미 없음)
  useEffect(() => {
    if (urlInitialPosition !== null) {
      setInitialPosition(urlInitialPosition);
      return;
    }

    if (
      !isLive &&
      watchHistory?.lastPosition &&
      watchHistory.lastPosition > 0
    ) {
      setInitialPosition(watchHistory.lastPosition);
    }
  }, [watchHistory, isLive, urlInitialPosition]);

  // 현재 재생 위치 ref 업데이트
  useEffect(() => {
    currentPositionRef.current = Math.floor(currentTimeMs / 1000);
  }, [currentTimeMs]);

  // 시청 위치 저장 함수
  const savePosition = useCallback(
    (position: number) => {
      if (!isAuthenticated) {
        return;
      }

      // 마지막 저장 위치와 동일하면 저장하지 않음
      if (position === lastSavedPositionRef.current || position <= 0) {
        return;
      }

      saveWatchHistory.mutate(
        { uuid: currentVideo.uuid, data: { position } },
        {
          onSuccess: () => {
            lastSavedPositionRef.current = position;
          },
        },
      );
    },
    [isAuthenticated, currentVideo.uuid, saveWatchHistory],
  );

  // savePosition은 매 렌더마다 새 참조가 되므로 ref에 담아둔다.
  // 타이머 effect가 savePosition에 의존하면 렌더마다 interval이 리셋되어 영원히 tick이 안 온다.
  const savePositionRef = useRef(savePosition);
  useEffect(() => {
    savePositionRef.current = savePosition;
  }, [savePosition]);

  // 10초마다 시청 위치 저장 (라이브는 제외).
  // isLive가 바뀔 때만 interval을 재생성한다.
  useEffect(() => {
    if (isLive) return;
    const interval = setInterval(() => {
      const currentPosition = currentPositionRef.current;
      if (currentPosition > 0) {
        savePositionRef.current(currentPosition);
      }
    }, 10000);

    return () => clearInterval(interval);
  }, [isLive]);

  // 시청자 수 이력 조회 (라이브 중에는 주기적 갱신)
  const { data: viewerHistory } = useGetVideoViewerHistory(currentVideo.uuid, {
    query: {
      refetchInterval: isLive ? LIVE_VIDEO_REFRESH_INTERVAL_MS : false,
      staleTime: isLive ? 0 : 60_000,
    },
  });

  // 챕터 조회 (카테고리 변경 이력 기반, 라이브 중에는 주기적 갱신)
  const { data: chapters } = useGetVideoChapters(currentVideo.uuid, {
    query: {
      refetchInterval: isLive ? LIVE_VIDEO_REFRESH_INTERVAL_MS : false,
      staleTime: isLive ? 0 : 60_000,
    },
  });

  // 와이드 모드: 사이드바를 가리고 좌측 비디오를 viewport 가득, 우측 채팅 유지
  const [isWide, setIsWide] = useState(false);

  // 타임라인 패널 열림 여부 (우측 채팅 영역을 덮는다)
  const [showTimeline, setShowTimeline] = useState(false);

  // 넓은 화면(와이드)으로 들어가면 타임라인 버튼이 사라지므로, 진입 시 타임라인을 닫는다.
  // (와이드는 비디오 몰입 모드. 와이드를 끄는 경우엔 타임라인 상태를 건드리지 않는다.)
  const handleWideToggle = useCallback((wide: boolean) => {
    setIsWide(wide);
    if (wide) setShowTimeline(false);
  }, []);

  const handleDescriptionTimestampClick = useCallback((seconds: number) => {
    setSeekRequest({ seconds, nonce: Date.now() });
  }, []);

  // 현재 시청자 수: 시청자 이력의 마지막 값
  const currentViewerCount = useMemo(() => {
    if (!viewerHistory || viewerHistory.length === 0) return 0;
    return viewerHistory[viewerHistory.length - 1].viewerCount;
  }, [viewerHistory]);

  // 플레이어 상단 헤더 정보
  const playerHeaderInfo = useMemo(
    () => ({
      title: currentVideo.title,
      channelName: currentVideo.channel.name,
      channelProfileUrl: currentVideo.channel.profileUrl,
      viewerCount: currentViewerCount,
      streamStartedAt: currentVideo.record?.startedAt,
    }),
    [
      currentVideo.title,
      currentVideo.channel.name,
      currentVideo.channel.profileUrl,
      currentViewerCount,
      currentVideo.record?.startedAt,
    ],
  );

  return (
    <div
      className={
        isWide
          ? "fixed inset-0 z-[60] bg-background flex flex-col lg:flex-row overflow-hidden"
          : "flex flex-col lg:flex-row h-[calc(100vh-4rem)] overflow-hidden"
      }
    >
      {/* 좌측: 동영상 + 정보 */}
      <div
        className={`flex-shrink-0 lg:flex-1 flex flex-col ${isWide ? "overflow-hidden" : "overflow-y-auto overscroll-contain scrollbar-hide"}`}
      >
        <VideoPlayer
          playlistUrl={currentVideo.playlistUrl}
          onTimeUpdate={setCurrentTimeMs}
          initialPosition={initialPosition}
          seekRequest={seekRequest}
          isLive={isLive}
          viewerHistory={viewerHistory}
          chapters={chapters}
          isWide={isWide}
          onWideToggle={handleWideToggle}
          headerInfo={playerHeaderInfo}
        />
        {!isWide && (
          <VideoInfo
            video={currentVideo}
            isAdmin={user?.role === "ADMIN"}
            onTimestampClick={handleDescriptionTimestampClick}
            isLive={isLive}
            viewerCount={currentViewerCount}
            chapters={chapters}
            currentTimeMs={currentTimeMs}
            onTimelineClick={() => setShowTimeline((prev) => !prev)}
          />
        )}
      </div>

      {/* 우측: 채팅창. 타임라인 버튼을 누르면 타임라인 패널이 그 위를 덮는다. (와이드 모드는 항상 다크) */}
      <div
        className={`flex-1 lg:flex-initial w-full lg:w-88 lg:h-full mt-3 lg:mt-0 min-h-0 relative ${isWide ? "dark bg-background text-foreground" : ""}`}
      >
        <ChatHistory
          videoUuid={currentVideo.uuid}
          currentTimeMs={currentTimeMs}
          chatSyncOffsetMillis={currentVideo.chatSyncOffsetMillis}
        />
        {showTimeline && (
          <div className="absolute inset-0 z-10">
            <TimelinePanel
              videoUuid={currentVideo.uuid}
              currentTimeMs={currentTimeMs}
              durationSec={currentVideo.duration}
              viewerHistory={viewerHistory}
              chapters={chapters}
              isLive={isLive}
              onClose={() => setShowTimeline(false)}
              onSeek={(offsetMillis) =>
                handleDescriptionTimestampClick(offsetMillis / 1000)
              }
            />
          </div>
        )}
      </div>
    </div>
  );
}
