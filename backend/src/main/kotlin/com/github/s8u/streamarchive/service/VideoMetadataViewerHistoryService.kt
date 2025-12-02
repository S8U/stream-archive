package com.github.s8u.streamarchive.service

import com.github.s8u.streamarchive.entity.VideoMetadataViewerHistory
import com.github.s8u.streamarchive.event.StreamDetectedEvent
import com.github.s8u.streamarchive.recorder.RecordProcessManager
import com.github.s8u.streamarchive.repository.RecordRepository
import com.github.s8u.streamarchive.repository.VideoMetadataViewerHistoryRepository
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

@Service
class VideoMetadataViewerHistoryService(
    private val viewerHistoryRepository: VideoMetadataViewerHistoryRepository,
    private val recordRepository: RecordRepository,
    private val recordProcessManager: RecordProcessManager
) {
    private val logger = LoggerFactory.getLogger(VideoMetadataViewerHistoryService::class.java)

    // 녹화한 마지막 시청자 수 <recordId, lastViewerCount>
    private val lastViewerCountCache = ConcurrentHashMap<Long, Int?>()

    /**
     * 마지막 시청자 수 캐시 정리
     */
    fun clearCache(recordId: Long) {
        lastViewerCountCache.remove(recordId)
    }

    /**
     * 시청자 수 저장
     */
    @Transactional
    fun saveViewerCount(recordId: Long, videoId: Long, viewerCount: Int?, offsetMillis: Long) {
        viewerCount?.let {
            viewerHistoryRepository.save(VideoMetadataViewerHistory(
                videoId = videoId,
                viewerCount = it,
                offsetMillis = offsetMillis
            ))
            lastViewerCountCache[recordId] = it
            logger.debug("Saved viewer count: recordId={}, count={}, offset={}", recordId, it, offsetMillis)
        }
    }

    /**
     * 녹화 중인 스트리밍의 시청자 수 저장
     */
    @Async
    @EventListener
    @Transactional
    fun handleStreamDetected(event: StreamDetectedEvent) {
        try {
            // 해당 채널+플랫폼의 녹화 중인 Record 조회
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

            val currentViewerCount = event.stream.viewerCount
            val lastViewerCount = lastViewerCountCache[activeRecord.id]

            // 시청자 수가 변경된 경우에만 저장
            if (currentViewerCount != null && currentViewerCount != lastViewerCount) {
                val offsetMillis = Duration.between(activeRecord.createdAt, LocalDateTime.now()).toMillis()
                saveViewerCount(activeRecord.id!!, activeRecord.videoId, currentViewerCount, offsetMillis)
            }
        } catch (e: Exception) {
            logger.error("Failed to save viewer history from event", e)
        }
    }

}
