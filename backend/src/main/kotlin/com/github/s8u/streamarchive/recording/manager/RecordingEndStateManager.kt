package com.github.s8u.streamarchive.recording.manager

import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

/**
 * 종료 처리 중인 녹화 상태 매니저
 *
 * 같은 녹화를 여러 스레드가 동시에 종료 처리하는 것을 막는다.
 * 종료 처리 중인 녹화는 메타데이터 갱신에서 제외해 트랜잭션 충돌을 피한다.
 */
@Component
class RecordingEndStateManager {

    private val endingRecords = ConcurrentHashMap.newKeySet<Long>()

    /**
     * 종료 처리 시작을 표시한다.
     *
     * 이미 처리 중이면 false를 반환한다.
     */
    fun markEnding(recordId: Long): Boolean {
        return endingRecords.add(recordId)
    }

    /**
     * 종료 처리 완료를 표시한다.
     */
    fun unmarkEnding(recordId: Long) {
        endingRecords.remove(recordId)
    }

    /**
     * [recordId] 녹화가 종료 처리 중인지 확인한다.
     */
    fun isEnding(recordId: Long): Boolean {
        return endingRecords.contains(recordId)
    }

}
