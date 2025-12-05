import type { ChatHistoryResponse } from '@/lib/api/models/chatHistoryResponse';

export interface ChatBucket {
    startTimeMs: number;
    endTimeMs: number;
    count: number;
}

export interface RSIData {
    timeMs: number;
    rsi: number;
}

export interface HighlightSegment {
    startTimeMs: number;
    endTimeMs: number;
    rsiScore: number;
}

/**
 * 채팅 데이터를 10초 단위로 묶기
 */
export function groupChatsByInterval(
    chats: ChatHistoryResponse[],
    intervalMs: number = 10000 // 10초
): ChatBucket[] {
    if (chats.length === 0) return [];

    const buckets: Map<number, number> = new Map();

    chats.forEach(chat => {
        const bucketIndex = Math.floor(chat.offsetMillis / intervalMs);
        buckets.set(bucketIndex, (buckets.get(bucketIndex) || 0) + 1);
    });

    // 빈 구간도 포함하여 연속적인 배열 생성
    const maxBucket = Math.max(...Array.from(buckets.keys()));
    const result: ChatBucket[] = [];

    for (let i = 0; i <= maxBucket; i++) {
        result.push({
            startTimeMs: i * intervalMs,
            endTimeMs: (i + 1) * intervalMs,
            count: buckets.get(i) || 0
        });
    }

    return result;
}

/**
 * RSI (Relative Strength Index) 계산
 * RSI는 0~100 사이의 값으로, 70 이상이면 과매수(하이라이트), 30 이하면 과매도
 */
export function calculateRSI(
    chatBuckets: ChatBucket[],
    period: number = 14 // RSI 기간 (기본 14)
): RSIData[] {
    if (chatBuckets.length < period + 1) {
        return chatBuckets.map(bucket => ({
            timeMs: bucket.startTimeMs,
            rsi: 50 // 기본값
        }));
    }

    const rsiData: RSIData[] = [];
    const changes: number[] = [];

    // 변화량 계산
    for (let i = 1; i < chatBuckets.length; i++) {
        changes.push(chatBuckets[i].count - chatBuckets[i - 1].count);
    }

    // RSI 계산
    for (let i = period; i < chatBuckets.length; i++) {
        const recentChanges = changes.slice(i - period, i);

        let gains = 0;
        let losses = 0;

        recentChanges.forEach(change => {
            if (change > 0) {
                gains += change;
            } else {
                losses += Math.abs(change);
            }
        });

        const avgGain = gains / period;
        const avgLoss = losses / period;

        let rsi: number;
        if (avgLoss === 0) {
            rsi = 100;
        } else {
            const rs = avgGain / avgLoss;
            rsi = 100 - (100 / (1 + rs));
        }

        rsiData.push({
            timeMs: chatBuckets[i].startTimeMs,
            rsi: rsi
        });
    }

    // 초반 데이터는 기본값으로 채우기
    for (let i = 0; i < period; i++) {
        rsiData.unshift({
            timeMs: chatBuckets[i].startTimeMs,
            rsi: 50
        });
    }

    return rsiData;
}

/**
 * RSI 기반으로 하이라이트 구간 추출
 * RSI가 50을 넘는 구간 중에서 60 이상의 피크가 있는 구간을 하이라이트로 추출
 */
export function extractHighlights(
    rsiData: RSIData[],
    chatBuckets: ChatBucket[],
    threshold: number = 60, // RSI 피크 임계값 (기본 60)
    baseThreshold: number = 50, // RSI 구간 시작/종료 임계값 (기본 50)
    minDurationMs: number = 10000, // 최소 하이라이트 길이 (10초)
    mergeGapMs: number = 20000 // 근접한 하이라이트 병합 간격 (20초)
): HighlightSegment[] {
    const highlights: HighlightSegment[] = [];
    let potentialSegment: { startTimeMs: number; endTimeMs: number; maxRsi: number } | undefined = undefined;
    let hasHighPeak = false; // 현재 구간에서 threshold를 넘었는지 여부

    rsiData.forEach((data, index) => {
        if (data.rsi >= baseThreshold) {
            if (!potentialSegment) {
                // RSI 50 이상 구간 시작
                potentialSegment = {
                    startTimeMs: data.timeMs,
                    endTimeMs: data.timeMs + 10000,
                    maxRsi: data.rsi
                };
                hasHighPeak = data.rsi >= threshold;
            } else {
                // 기존 구간 확장
                potentialSegment.endTimeMs = data.timeMs + 10000;
                potentialSegment.maxRsi = Math.max(potentialSegment.maxRsi, data.rsi);

                // 피크 체크
                if (data.rsi >= threshold) {
                    hasHighPeak = true;
                }
            }
        } else {
            // RSI가 50 아래로 떨어짐 - 구간 종료
            if (potentialSegment && hasHighPeak) {
                // 60 이상의 피크가 있었던 구간만 하이라이트로 추가
                if (potentialSegment.endTimeMs - potentialSegment.startTimeMs >= minDurationMs) {
                    highlights.push({
                        startTimeMs: potentialSegment.startTimeMs,
                        endTimeMs: potentialSegment.endTimeMs,
                        rsiScore: potentialSegment.maxRsi
                    });
                }
            }
            potentialSegment = undefined;
            hasHighPeak = false;
        }
    });

    // 마지막 세그먼트 처리
    if (potentialSegment && hasHighPeak) {
        if (potentialSegment.endTimeMs - potentialSegment.startTimeMs >= minDurationMs) {
            highlights.push({
                startTimeMs: potentialSegment.startTimeMs,
                endTimeMs: potentialSegment.endTimeMs,
                rsiScore: potentialSegment.maxRsi
            });
        }
    }

    // 근접한 하이라이트 병합
    if (highlights.length === 0) {
        return [];
    }

    const mergedHighlights: HighlightSegment[] = [];
    let current = highlights[0];

    for (let i = 1; i < highlights.length; i++) {
        const next = highlights[i];

        if (next.startTimeMs - current.endTimeMs <= mergeGapMs) {
            // 병합
            current = {
                startTimeMs: current.startTimeMs,
                endTimeMs: next.endTimeMs,
                rsiScore: Math.max(current.rsiScore, next.rsiScore)
            };
        } else {
            mergedHighlights.push(current);
            current = next;
        }
    }

    mergedHighlights.push(current);

    return mergedHighlights;
}

/**
 * 전체 채팅 데이터로부터 하이라이트 추출 (올인원 함수)
 */
export function generateHighlights(
    chats: ChatHistoryResponse[],
    options?: {
        intervalMs?: number;
        rsiPeriod?: number;
        rsiThreshold?: number;
        rsiBaseThreshold?: number;
        minDurationMs?: number;
        mergeGapMs?: number;
    }
): {
    chatBuckets: ChatBucket[];
    rsiData: RSIData[];
    highlights: HighlightSegment[];
} {
    const chatBuckets = groupChatsByInterval(chats, options?.intervalMs);
    const rsiData = calculateRSI(chatBuckets, options?.rsiPeriod);
    const highlights = extractHighlights(
        rsiData,
        chatBuckets,
        options?.rsiThreshold,
        options?.rsiBaseThreshold,
        options?.minDurationMs,
        options?.mergeGapMs
    );

    return { chatBuckets, rsiData, highlights };
}
