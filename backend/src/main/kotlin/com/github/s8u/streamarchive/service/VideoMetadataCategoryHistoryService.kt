package com.github.s8u.streamarchive.service

import com.github.s8u.streamarchive.entity.VideoMetadataCategoryHistory
import com.github.s8u.streamarchive.event.StreamDetectedEvent
import com.github.s8u.streamarchive.recorder.RecordProcessManager
import com.github.s8u.streamarchive.repository.RecordRepository
import com.github.s8u.streamarchive.repository.VideoMetadataCategoryHistoryRepository
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

@Service
class VideoMetadataCategoryHistoryService(
    private val categoryHistoryRepository: VideoMetadataCategoryHistoryRepository,
    private val recordRepository: RecordRepository,
    private val recordProcessManager: RecordProcessManager
) {
    private val logger = LoggerFactory.getLogger(VideoMetadataCategoryHistoryService::class.java)

    // 녹화한 마지막 카테고리 <recordId, lastCategory>
    private val lastCategoryCache = ConcurrentHashMap<Long, String?>()

    /**
     * 마지막 카테고리 캐시 삭제
     */
    fun clearCache(recordId: Long) {
        lastCategoryCache.remove(recordId)
    }

    /**
     * 카테고리 저장
     */
    @Transactional
    fun saveCategory(recordId: Long, videoId: Long, category: String?, offsetMillis: Long) {
        categoryHistoryRepository.save(VideoMetadataCategoryHistory(
            videoId = videoId,
            category = category,
            offsetMillis = offsetMillis
        ))
        lastCategoryCache[recordId] = category
        logger.info("Saved category: recordId={}, category={}, offset={}", recordId, category, offsetMillis)
    }

    /**
     * 녹화 중인 스트리밍의 카테고리 저장
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

            // 현재 서버에서 녹화 중인지 확인 (다중 서버 대비)
            if (!recordProcessManager.isProcessAlive(activeRecord.id!!)) {
                logger.debug("Record is not running on this server: recordId={}", activeRecord.id)
                return
            }

            val currentCategory = event.stream.category
            val lastCategory = lastCategoryCache[activeRecord.id]

            // 카테고리가 변경된 경우에만 저장
            if (currentCategory != lastCategory) {
                val offsetMillis = Duration.between(activeRecord.createdAt, LocalDateTime.now()).toMillis()
                saveCategory(activeRecord.id!!, activeRecord.videoId, currentCategory, offsetMillis)
            }
        } catch (e: Exception) {
            logger.error("Failed to save category history from event", e)
        }
    }

}
