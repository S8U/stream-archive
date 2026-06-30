"use client";

import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { useGetVideoChatAnalysis } from "@/lib/api/endpoints/video/video";
import type {
  VideoChapterGetResponse,
  VideoViewerHistoryGetResponse,
} from "@/lib/api/models";
import { toDisplayChapters, type DisplayChapter } from "@/lib/chapters";
import { useEffect, useMemo, useRef, useState } from "react";

// 오프셋(ms)을 방송 경과 시간 문자열로 변환한다.
function formatOffset(milliseconds: number): string {
  const totalSeconds = Math.floor(milliseconds / 1000);
  const hours = Math.floor(totalSeconds / 3600);
  const minutes = Math.floor((totalSeconds % 3600) / 60);
  const seconds = totalSeconds % 60;

  if (hours > 0) {
    return `${hours}:${minutes.toString().padStart(2, "0")}:${seconds.toString().padStart(2, "0")}`;
  }
  return `${minutes}:${seconds.toString().padStart(2, "0")}`;
}

// 라벨이 최대 글자 수를 넘으면 잘라낸다(좁은 패널에서 가로 넘침 방지).
function clipLabel(label: string, maxChars: number): string {
  return label.length > maxChars ? label.slice(0, maxChars) + "…" : label;
}

// 세로 그래프 레이아웃 (원본 짤처럼 시간이 위→아래로 흐른다)
const ROW_HEIGHT = 22; // 버킷 1개당 세로 픽셀
const LABEL_GAP = 8; // 그래프와 텍스트 주석 사이 간격
const TOP_PAD = 8;
const BOTTOM_PAD = 8;
const TIME_TICK_EVERY = 5; // 시간 눈금을 몇 버킷마다 찍을지
const LABEL_CHAR_WIDTH = 6.5; // 라벨 글자 한 칸의 대략 픽셀(잘림 계산용)
const CHAPTER_CHIP_HEIGHT = 16; // 챕터 라벨 칩 높이
const CHAPTER_CHIP_CHAR_WIDTH = 5.6; // 칩 텍스트(10px bold) 글자 한 칸의 대략 픽셀

// 그래프 배치 두 종류 (영상 아래 넓은 배치 / 우측 패널 좁은 배치)
// viewWidth는 패널 실제 폭(lg:w-88 = 352px)에 맞춘다.
const WIDE_LAYOUT = { viewWidth: 720, axisWidth: 56, graphWidth: 180 };
const PANEL_LAYOUT = { viewWidth: 352, axisWidth: 40, graphWidth: 90 };
// 1시간 이상이면 시간 라벨이 H:MM:SS로 길어지므로 좌측 축을 이만큼 더 넓힌다.
const AXIS_WIDTH_EXTRA_FOR_HOURS = 16;

// 챕터 색상 팔레트 (카테고리 등장 순서대로 순환 배정).
// 그래프 색(chart-1·chart-4는 모두 파랑 계열)과 섞이지 않도록 따뜻한 계열 고정값을 쓴다.
// 다크/라이트 양쪽에서 또렷하게 보이도록 채도를 높게 둔다.
const CHAPTER_COLORS = [
  "oklch(0.70 0.18 40)", // 주황
  "oklch(0.72 0.16 130)", // 초록
  "oklch(0.70 0.17 350)", // 로즈
  "oklch(0.78 0.15 85)", // 황토
];

// 카테고리 이름으로 챕터 색을 정한다.
// 등장 순서가 아니라 이름 해시로 팔레트를 고르므로, 같은 카테고리("리그 오브 레전드" 등)는
// 어느 영상에서든·구간 순서와 무관하게 항상 같은 색이 된다.
function chapterColorFor(name: string): string {
  let hash = 0;
  for (let i = 0; i < name.length; i++) {
    // djb2 변형. 32비트 정수로 유지한다.
    hash = (hash * 31 + name.charCodeAt(i)) | 0;
  }
  return CHAPTER_COLORS[Math.abs(hash) % CHAPTER_COLORS.length];
}

// 라이브 중에는 채팅이 계속 쌓이므로 주기적으로 다시 분석한다.
// 전 구간 재분석이라 viewer 수(10초)보다 느슨하게 둔다.
const LIVE_ANALYSIS_REFRESH_INTERVAL_MS = 30_000;

interface TimelineChartProps {
  videoUuid: string;
  /** 라이브(녹화 중)면 주기적으로 다시 분석한다. */
  isLive?: boolean;
  /** 현재 재생 위치(밀리초). 해당 구간을 강조한다. */
  currentTimeMs?: number;
  /** 재생 시간(초). 챕터 마지막 구간의 끝을 잡는 데 쓴다. */
  durationSec?: number;
  /** 시청자 수 이력. 채팅 수 그래프 위에 겹쳐 그린다. */
  viewerHistory?: VideoViewerHistoryGetResponse[];
  /** 카테고리 변경 이력(챕터). 시간축 옆 세로 띠로 표시한다. */
  chapters?: VideoChapterGetResponse[];
  /** 패널에 끼울 때는 Card·헤더 없이 부모 높이를 채운다. */
  embedded?: boolean;
  /** 차트 구간을 클릭하면 해당 시점으로 이동시킨다. */
  onSeek?: (offsetMillis: number) => void;
}

// 토글 가능한 레이어 종류.
type LayerKey = "chat" | "keyword" | "viewer" | "chapter";

export function TimelineChart({
  videoUuid,
  isLive = false,
  currentTimeMs,
  durationSec,
  viewerHistory,
  chapters,
  embedded = false,
  onSeek,
}: TimelineChartProps) {
  const { data, isLoading } = useGetVideoChatAnalysis(videoUuid, undefined, {
    query: {
      refetchInterval: isLive ? LIVE_ANALYSIS_REFRESH_INTERVAL_MS : false,
      staleTime: isLive ? 0 : 60_000,
    },
  });

  // 레이어 토글 상태. 기본은 전부 켜짐.
  const [layers, setLayers] = useState<Record<LayerKey, boolean>>({
    chat: true,
    keyword: true,
    viewer: true,
    chapter: true,
  });
  const toggleLayer = (key: LayerKey) =>
    setLayers((prev) => ({ ...prev, [key]: !prev[key] }));

  const buckets = useMemo(() => data?.buckets ?? [], [data]);
  const hasData = buckets.length > 0;
  const bucketMillis = data?.bucketMillis ?? 0;

  // 현재 재생 위치가 속한 버킷 인덱스 (강조용).
  const currentBucketIndex = useMemo(() => {
    if (currentTimeMs == null || bucketMillis <= 0) return -1;
    return Math.floor(currentTimeMs / bucketMillis);
  }, [currentTimeMs, bucketMillis]);

  // 전체 시간 길이(ms). duration 우선, 없으면(라이브 등) 데이터의 마지막 오프셋으로 폴백.
  const totalMillis = useMemo(() => {
    const fromDuration = durationSec && durationSec > 0 ? durationSec * 1000 : 0;
    const lastBucket = buckets.length
      ? buckets[buckets.length - 1].offsetMillis + bucketMillis
      : 0;
    const lastViewer = viewerHistory?.length
      ? viewerHistory[viewerHistory.length - 1].offsetMillis
      : 0;
    return Math.max(fromDuration, lastBucket, lastViewer);
  }, [durationSec, buckets, bucketMillis, viewerHistory]);

  // 좁은 우측 패널이면 그래프·라벨 폭을 줄인 배치를 쓴다.
  const baseLayout = embedded ? PANEL_LAYOUT : WIDE_LAYOUT;
  // 1시간을 넘으면 시간 라벨이 M:SS → H:MM:SS로 길어진다.
  // 그만큼 좌측 축 폭을 넓혀 라벨이 잘리지 않게 하고, 늘어난 만큼 그래프 폭을 줄여 총폭을 유지한다.
  const layout = useMemo(() => {
    if (totalMillis < 3_600_000) return baseLayout;
    const extra = AXIS_WIDTH_EXTRA_FOR_HOURS;
    return {
      ...baseLayout,
      axisWidth: baseLayout.axisWidth + extra,
      graphWidth: baseLayout.graphWidth - extra,
    };
  }, [baseLayout, totalMillis]);
  // 좌측부터: 시간 눈금 → 그래프 → 라벨 순으로 가로를 나눈다.
  // 챕터는 별도 가로 칸을 차지하지 않고 전환 지점에 풀폭 구분선 + 라벨 칩으로 얹는다.
  const graphLeft = layout.axisWidth;
  const graphRight = graphLeft + layout.graphWidth;
  const labelLeft = graphRight + LABEL_GAP;
  // 라벨이 들어갈 가로 공간을 글자 수로 환산한다.
  const labelMaxChars = Math.floor(
    (layout.viewWidth - labelLeft) / LABEL_CHAR_WIDTH,
  );

  const maxCount = useMemo(
    () => buckets.reduce((m, b) => Math.max(m, b.count), 0),
    [buckets],
  );

  // 각 버킷의 y중심 좌표와 채팅 수 그래프 폭을 미리 계산한다.
  const points = useMemo(() => {
    return buckets.map((b, i) => ({
      offsetMillis: b.offsetMillis,
      time: formatOffset(b.offsetMillis),
      count: b.count,
      keywords: b.keywords,
      y: TOP_PAD + i * ROW_HEIGHT + ROW_HEIGHT / 2,
      width: maxCount > 0 ? (b.count / maxCount) * layout.graphWidth : 0,
      isCurrent: i === currentBucketIndex,
    }));
  }, [buckets, maxCount, currentBucketIndex, layout.graphWidth]);

  const svgHeight = TOP_PAD + buckets.length * ROW_HEIGHT + BOTTOM_PAD;

  // 오프셋(ms)을 세로 y좌표로 변환한다(시청자 라인·챕터 띠가 채팅 버킷과 같은 축을 쓰도록).
  const offsetToY = useMemo(() => {
    const usableHeight = svgHeight - TOP_PAD - BOTTOM_PAD;
    return (offsetMillis: number) => {
      if (totalMillis <= 0) return TOP_PAD;
      const ratio = Math.min(1, Math.max(0, offsetMillis / totalMillis));
      return TOP_PAD + ratio * usableHeight;
    };
  }, [svgHeight, totalMillis]);

  // 현재 재생 위치 버킷의 y좌표 (강조선·자동 스크롤용).
  const currentY = useMemo(() => {
    const point = points.find((p) => p.isCurrent);
    return point?.y ?? null;
  }, [points]);

  // 재생 위치가 스크롤 영역 밖으로 나가면 가운데로 따라가게 한다(수동 스크롤은 최대한 방해하지 않는다).
  const scrollRef = useRef<HTMLDivElement>(null);
  useEffect(() => {
    const container = scrollRef.current;
    if (container == null || currentY == null) return;

    const viewTop = container.scrollTop;
    const viewBottom = viewTop + container.clientHeight;
    if (currentY < viewTop || currentY > viewBottom) {
      container.scrollTo({
        top: currentY - container.clientHeight / 2,
        behavior: "smooth",
      });
    }
  }, [currentY]);

  // 채팅 수 영역 path: 좌측 기준선(graphLeft)에서 시작해 각 버킷의 폭만큼 오른쪽으로 뻗는다.
  const chatAreaPath = useMemo(() => {
    if (points.length === 0) return "";
    const top = points[0].y;
    const bottom = points[points.length - 1].y;
    const right = points
      .map((p) => `L ${graphLeft + p.width} ${p.y}`)
      .join(" ");
    return `M ${graphLeft} ${top} ${right} L ${graphLeft} ${bottom} Z`;
  }, [points, graphLeft]);

  // 시청자 수 라인: 자기 최댓값으로 따로 정규화해 같은 그래프 영역에 겹쳐 그린다.
  const viewerLinePath = useMemo(() => {
    if (!viewerHistory || viewerHistory.length < 2 || totalMillis <= 0) {
      return "";
    }
    const maxViewer = viewerHistory.reduce(
      (m, v) => Math.max(m, v.viewerCount),
      0,
    );
    if (maxViewer <= 0) return "";
    return viewerHistory
      .map((v, i) => {
        const x = graphLeft + (v.viewerCount / maxViewer) * layout.graphWidth;
        const y = offsetToY(v.offsetMillis);
        return `${i === 0 ? "M" : "L"} ${x} ${y}`;
      })
      .join(" ");
  }, [viewerHistory, totalMillis, graphLeft, layout.graphWidth, offsetToY]);

  // 표시용 챕터(카테고리 구간)와 카테고리별 색상 매핑.
  const displayChapters = useMemo<DisplayChapter[]>(
    () => toDisplayChapters(chapters, totalMillis / 1000),
    [chapters, totalMillis],
  );
  // 카테고리(없으면 라벨) 이름 해시로 색을 정한다. 같은 카테고리면 항상 같은 색.
  const chapterColorOf = (chapter: DisplayChapter) =>
    chapterColorFor(chapter.category?.trim() || chapter.label);

  // 텍스트 주석을 단 피크 구간 (키워드가 있는 버킷)
  const peaks = useMemo(
    () => points.filter((p) => p.keywords.length > 0),
    [points],
  );

  const content = isLoading ? (
    <Skeleton className="h-[400px] w-full" />
  ) : !hasData ? (
    <p className="text-muted-foreground py-12 text-center text-sm">
      채팅 데이터가 없습니다.
    </p>
  ) : (
    <div
      ref={scrollRef}
      className={
        embedded ? "h-full overflow-auto" : "max-h-[600px] overflow-auto"
      }
    >
      <svg
        width="100%"
        height={svgHeight}
        viewBox={`0 0 ${layout.viewWidth} ${svgHeight}`}
        preserveAspectRatio="xMinYMin meet"
        className={embedded ? undefined : "min-w-[680px]"}
      >
        {/* 시간 눈금 (좌측) + 가로 보조선 */}
        {points.map((p, i) =>
          i % TIME_TICK_EVERY === 0 ? (
            <g key={`tick-${p.offsetMillis}`}>
              <line
                x1={graphLeft}
                y1={p.y}
                x2={graphRight}
                y2={p.y}
                className="stroke-border"
                strokeWidth={1}
                strokeDasharray="2 3"
              />
              <text
                x={graphLeft - 6}
                y={p.y}
                textAnchor="end"
                dominantBaseline="middle"
                className="fill-muted-foreground font-mono text-[10px] tabular-nums"
              >
                {p.time}
              </text>
            </g>
          ) : null,
        )}

        {/* 분당 채팅 수 영역 그래프 */}
        {layers.chat && (
          <path
            d={chatAreaPath}
            fill="var(--chart-1)"
            fillOpacity={0.3}
            stroke="var(--chart-1)"
            strokeWidth={1.5}
          />
        )}

        {/* 시청자 수 라인 (채팅 영역 위에 겹침, 자기 최댓값 기준 정규화) */}
        {layers.viewer && viewerLinePath && (
          <path
            d={viewerLinePath}
            fill="none"
            stroke="var(--chart-4)"
            strokeWidth={1.5}
            strokeOpacity={0.9}
          />
        )}

        {/* 현재 재생 위치 강조 (가로 강조선 + 좌측 마커) */}
        {currentY != null && (
          <g>
            <line
              x1={0}
              y1={currentY}
              x2={layout.viewWidth}
              y2={currentY}
              stroke="var(--primary)"
              strokeWidth={1.5}
              strokeOpacity={0.7}
            />
            <polygon
              points={`2,${currentY - 5} 10,${currentY} 2,${currentY + 5}`}
              fill="var(--primary)"
            />
          </g>
        )}

        {/* 채팅 수 그래프 끝점에 찍는 점 (피크 구간 표시, 채팅 수 레이어 소속) */}
        {layers.chat &&
          peaks.map((p) => (
            <circle
              key={`peak-dot-${p.offsetMillis}`}
              cx={graphLeft + p.width}
              cy={p.y}
              r={2.5}
              fill="var(--chart-1)"
            />
          ))}

        {/* 주요 채팅 텍스트 주석 (피크 구간 옆) */}
        {layers.keyword &&
          peaks.map((p) => (
            <text
              key={`label-${p.offsetMillis}`}
              x={labelLeft}
              y={p.y}
              dominantBaseline="middle"
              className={
                p.isCurrent
                  ? "fill-primary cursor-pointer text-[12px] font-bold"
                  : "fill-foreground cursor-pointer text-[12px]"
              }
              onClick={() => onSeek?.(p.offsetMillis)}
            >
              {clipLabel(
                p.keywords.map((k) => k.label).join("  "),
                labelMaxChars,
              )}
            </text>
          ))}

        {/* 챕터: 전환 지점 풀폭 구분선 + 라벨 칩 (그래프 위에 올려 묻히지 않게 맨 마지막에 그린다) */}
        {layers.chapter &&
          displayChapters.map((chapter) => {
            const lineY = offsetToY(chapter.startSec * 1000);
            // 칩이 viewBox 위로 잘리지 않게 y를 클램프한다(첫 챕터 0:00 대응).
            const chipY = Math.max(lineY, TOP_PAD + CHAPTER_CHIP_HEIGHT / 2 + 1);
            const color = chapterColorOf(chapter);
            const text = `${formatOffset(chapter.startSec * 1000)}  ${clipLabel(chapter.label, 16)}`;
            const chipWidth = text.length * CHAPTER_CHIP_CHAR_WIDTH + 12;
            return (
              <g
                key={`chapter-${chapter.startSec}`}
                className="cursor-pointer"
                onClick={() => onSeek?.(chapter.startSec * 1000)}
              >
                {/* 전환 지점 풀폭 구분선 */}
                <line
                  x1={0}
                  y1={lineY}
                  x2={layout.viewWidth}
                  y2={lineY}
                  stroke={color}
                  strokeWidth={2}
                />
                {/* 라벨 칩 (구분선 위에 얹은 둥근 배경 + 굵은 텍스트) */}
                <rect
                  x={graphLeft}
                  y={chipY - CHAPTER_CHIP_HEIGHT / 2}
                  width={chipWidth}
                  height={CHAPTER_CHIP_HEIGHT}
                  rx={CHAPTER_CHIP_HEIGHT / 2}
                  fill={color}
                />
                <text
                  x={graphLeft + 7}
                  y={chipY}
                  dominantBaseline="middle"
                  className="fill-background text-[10px] font-bold"
                >
                  {text}
                </text>
              </g>
            );
          })}
      </svg>
    </div>
  );

  // 레이어 토글 칩. 시청자·챕터 데이터가 없으면 해당 칩은 숨긴다.
  const hasViewer = (viewerHistory?.length ?? 0) >= 2;
  const hasChapter = displayChapters.length > 0;
  // 맥락(챕터) → 시청자 수 → 채팅 수 → 주요 채팅 순.
  // 채팅 수와 그 설명인 주요 채팅을 인접하게 둔다.
  const chips: { key: LayerKey; label: string; color: string }[] = [
    ...(hasChapter
      ? [{ key: "chapter" as const, label: "챕터", color: CHAPTER_COLORS[0] }]
      : []),
    ...(hasViewer
      ? [{ key: "viewer" as const, label: "시청자 수", color: "var(--chart-4)" }]
      : []),
    { key: "chat", label: "채팅 수", color: "var(--chart-1)" },
    { key: "keyword", label: "주요 채팅", color: "var(--foreground)" },
  ];

  const toggleBar = hasData ? (
    <div className="flex flex-wrap gap-1.5 px-3 py-2">
      {chips.map((chip) => {
        const on = layers[chip.key];
        return (
          <button
            key={chip.key}
            type="button"
            onClick={() => toggleLayer(chip.key)}
            aria-pressed={on}
            className={`flex cursor-pointer items-center gap-1.5 rounded-full border px-2.5 py-1 text-xs transition-colors ${
              on
                ? "border-transparent bg-muted text-foreground"
                : "border-border text-muted-foreground"
            }`}
          >
            <span
              className="h-2 w-2 rounded-full"
              style={{ backgroundColor: on ? chip.color : "transparent", border: `1px solid ${chip.color}` }}
            />
            {chip.label}
          </button>
        );
      })}
    </div>
  ) : null;

  // 패널에 끼울 때(embedded)는 Card·헤더 없이 토글 바 + 차트를 부모 높이에 맞춘다.
  if (embedded) {
    return (
      <div className="flex h-full flex-col">
        {toggleBar}
        <div className="min-h-0 flex-1">{content}</div>
      </div>
    );
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>타임라인</CardTitle>
        <CardDescription>
          분당 채팅 수·시청자 수·챕터를 한 시간축에 겹쳐 본다
        </CardDescription>
      </CardHeader>
      <CardContent>
        {toggleBar}
        {content}
      </CardContent>
    </Card>
  );
}
