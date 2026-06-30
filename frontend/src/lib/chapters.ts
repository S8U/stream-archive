import type { VideoChapterGetResponse } from '@/lib/api/models';

/**
 * 챕터가 이보다 적으면 타임라인/목록에 표시하지 않는다.
 *
 * 카테고리 변경 시점이 곧 챕터라, 변경이 한 번도 없는 영상도 챕터 1개(전체 = 한 카테고리)로 본다.
 * 카테고리가 무엇인지는 늘 알려주는 게 일관되므로, 챕터가 1개라도 표시한다(0개일 때만 숨김).
 */
export const MIN_CHAPTER_COUNT = 1;

/** 화면 표시용으로 가공된 챕터. 시작·끝을 초 단위로 들고, 라벨을 미리 계산해 둔다. */
export interface DisplayChapter {
    /** 시작 위치 (초) */
    startSec: number;
    /** 끝 위치 (초). 마지막 챕터는 영상 duration. */
    endSec: number;
    /** 챕터 라벨 (카테고리 우선, 없으면 제목, 둘 다 없으면 '챕터 N') */
    label: string;
    category?: string;
    title?: string;
}

function toLabel(chapter: VideoChapterGetResponse, index: number): string {
    return chapter.category?.trim() || chapter.title?.trim() || `챕터 ${index + 1}`;
}

/**
 * API 챕터 응답을 표시용 챕터로 변환한다.
 *
 * - duration이 없거나(0) 챕터가 {@link MIN_CHAPTER_COUNT}개 미만이면 빈 배열을 반환해 UI에서 숨긴다.
 * - offsetMillis(오름차순 전제)로 각 챕터의 끝을 다음 챕터 시작 = 자기 끝으로 잡고, 마지막은 duration.
 * - duration을 넘는 시작 오프셋은 버린다 (재생 시간보다 뒤에 잡힌 이력 방어).
 */
export function toDisplayChapters(
    chapters: VideoChapterGetResponse[] | undefined,
    durationSec: number,
): DisplayChapter[] {
    if (!chapters || chapters.length < MIN_CHAPTER_COUNT) return [];
    if (!Number.isFinite(durationSec) || durationSec <= 0) return [];

    const sorted = [...chapters].sort((a, b) => a.offsetMillis - b.offsetMillis);

    const result: DisplayChapter[] = [];
    for (let i = 0; i < sorted.length; i++) {
        const startSec = sorted[i].offsetMillis / 1000;
        if (startSec >= durationSec) break;
        const nextStartSec = i + 1 < sorted.length ? sorted[i + 1].offsetMillis / 1000 : durationSec;
        const endSec = Math.min(nextStartSec, durationSec);
        if (endSec <= startSec) continue;
        result.push({
            startSec,
            endSec,
            label: toLabel(sorted[i], i),
            category: sorted[i].category,
            title: sorted[i].title,
        });
    }

    // duration 초과분이 잘려 나가 남는 구간이 없으면 표시하지 않는다.
    return result.length < MIN_CHAPTER_COUNT ? [] : result;
}

/** 현재 재생 위치(초)가 속한 챕터를 찾는다. 없으면 null. */
export function findCurrentChapter(chapters: DisplayChapter[], currentSec: number): DisplayChapter | null {
    for (const chapter of chapters) {
        if (currentSec >= chapter.startSec && currentSec < chapter.endSec) {
            return chapter;
        }
    }
    // 마지막 챕터 끝(= duration)에 정확히 도달한 경우를 위해 마지막 챕터로 폴백.
    const last = chapters[chapters.length - 1];
    return last && currentSec >= last.startSec ? last : null;
}
