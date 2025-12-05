export interface HighlightSegment {
  startOffsetMs: number;
  endOffsetMs: number;
  chatCount: number;
  peakChatRate: number;
}

export interface HighlightParameters {
  windowSizeSeconds: number;
  thresholdPercentile: number;
  minSegmentSeconds: number;
  mergeGapSeconds: number;
}

export interface VideoHighlightsResponse {
  videoUuid: string;
  totalDurationMs: number;
  highlights: HighlightSegment[];
  totalHighlightDurationMs: number;
  parameters: HighlightParameters;
}

export interface GetVideoHighlightsParams {
  windowSize?: number;
  percentile?: number;
  minSegment?: number;
  mergeGap?: number;
}
