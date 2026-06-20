package com.github.s8u.streamarchive.recording.manager

import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

/**
 * 녹화 중인 동영상 카테고리 변경 감지 매니저
 *
 * 마지막으로 기록한 카테고리를 녹화별로 보관한다.
 * 폴링마다 직전 값과 비교해 변경 여부를 판단한다.
 * 녹화가 끝나면 정리한다.
 */
@Component
class RecordingVideoCategoryChangeDetectManager {

    private val lastCategoryCache = ConcurrentHashMap<Long, String?>()

    /**
     * 마지막으로 기록한 카테고리를 반환한다.
     */
    fun getLast(recordId: Long): String? {
        return lastCategoryCache[recordId]
    }

    /**
     * 마지막으로 기록한 카테고리를 갱신한다.
     */
    fun update(recordId: Long, category: String?) {
        lastCategoryCache[recordId] = category
    }

    /**
     * 녹화의 직전 값을 정리한다.
     */
    fun clear(recordId: Long) {
        lastCategoryCache.remove(recordId)
    }

}
