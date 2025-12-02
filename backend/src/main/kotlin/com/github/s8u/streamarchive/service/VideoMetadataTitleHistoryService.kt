package com.github.s8u.streamarchive.service

import com.github.s8u.streamarchive.entity.VideoMetadataTitleHistory
import com.github.s8u.streamarchive.event.StreamDetectedEvent
import com.github.s8u.streamarchive.recorder.RecordProcessManager
import com.github.s8u.streamarchive.repository.RecordRepository
import com.github.s8u.streamarchive.repository.VideoMetadataTitleHistoryRepository
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

@Service
class VideoMetadataTitleHistoryService(
    private val titleHistoryRepository: VideoMetadataTitleHistoryRepository,
    private val recordRepository: RecordRepository,
    private val recordProcessManager: RecordProcessManager
) {
    private val logger = LoggerFactory.getLogger(VideoMetadataTitleHistoryService::class.java)

    // 녹화한 마지막 제목 <recordId, lastTitle>
    private val lastTitleCache = ConcurrentHashMap<Long, String?>()

    /**
     * 마지막 제목 캐시 삭제
     */
    fun clearCache(recordId: Long) {
        lastTitleCache.remove(recordId)
    }

    /**
     * 제목 저장
     */
    @Transactional
    fun saveTitle(recordId: Long, videoId: Long, title: String?, offsetMillis: Long) {
        title?.let {
            titleHistoryRepository.save(VideoMetadataTitleHistory(
                videoId = videoId,
                title = it,
                offsetMillis = offsetMillis
            ))
            lastTitleCache[recordId] = it
            logger.info("Saved title: recordId={}, title={}, offset={}", recordId, it, offsetMillis)
        }
    }

    /**
     * 녹화 중인 스트리밍의 제목 저장
     */
    @Async
    @EventListener
    @Transactional
    fun handleStreamDetected(event: StreamDetectedEvent) {
        try {
            val activeRecord = recordRepository.findByChannelIdAndPlatformTypeAndIsEndedAndIsCancelled(
                channelId = event.channelPlatform.channel?.id ?: return,
                platformType = event.channelPlatform.platformType,
                isEnded = false,
                isCancelled = false
            ) ?: return

            // 현재 서버에서 녹화 중인지 확인
            if (!recordProcessManager.isProcessAlive(activeRecord.id!!)) {
                logger.debug("Record is not running on this server: recordId={}", activeRecord.id)
                return
            }

            val currentTitle = event.stream.title
            val lastTitle = lastTitleCache[activeRecord.id]

            // 제목이 변경된 경우에만 저장
            if (currentTitle != null && currentTitle != lastTitle) {
                val offsetMillis = Duration.between(activeRecord.createdAt, LocalDateTime.now()).toMillis()
                saveTitle(activeRecord.id!!, activeRecord.videoId, currentTitle, offsetMillis)
            }
        } catch (e: Exception) {
            logger.error("Failed to save title history from event", e)
        }
    }

}
