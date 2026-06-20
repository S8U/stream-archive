package com.github.s8u.streamarchive.recording.manager

import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

/**
 * 녹화 중인 동영상 시청자 수 변경 감지 매니저
 *
 * 마지막으로 기록한 시청자 수를 녹화별로 보관한다.
 * 폴링마다 직전 값과 비교해 변경 여부를 판단한다.
 * 녹화가 끝나면 정리한다.
 */
@Component
class RecordingVideoViewerChangeDetectManager {

    private val lastViewerCountCache = ConcurrentHashMap<Long, Int?>()

    /**
     * 마지막으로 기록한 시청자 수를 반환한다.
     */
    fun getLast(recordId: Long): Int? {
        return lastViewerCountCache[recordId]
    }

    /**
     * 마지막으로 기록한 시청자 수를 갱신한다.
     */
    fun update(recordId: Long, viewerCount: Int?) {
        lastViewerCountCache[recordId] = viewerCount
    }

    /**
     * 녹화의 직전 값을 정리한다.
     */
    fun clear(recordId: Long) {
        lastViewerCountCache.remove(recordId)
    }

}
