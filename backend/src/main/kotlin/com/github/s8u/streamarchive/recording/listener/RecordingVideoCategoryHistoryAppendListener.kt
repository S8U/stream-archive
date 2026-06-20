package com.github.s8u.streamarchive.recording.listener

import com.github.s8u.streamarchive.detect.event.StreamDetectedEvent
import com.github.s8u.streamarchive.recording.manager.RecordingVideoCategoryChangeDetectManager
import com.github.s8u.streamarchive.recording.manager.RecordingVideoProcessManager
import com.github.s8u.streamarchive.record.repository.RecordRepository
import com.github.s8u.streamarchive.video.service.VideoCategoryHistoryAppendService
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDateTime

/**
 * 녹화 중인 스트리밍의 카테고리를 저장하는 리스너
 *
 * 카테고리가 변경된 경우에만 저장한다.
 */
@Component
class RecordingVideoCategoryHistoryAppendListener(
    private val recordRepository: RecordRepository,
    private val recordingVideoProcessManager: RecordingVideoProcessManager,
    private val categoryHistoryAppendService: VideoCategoryHistoryAppendService,
    private val recordingVideoCategoryChangeDetectManager: RecordingVideoCategoryChangeDetectManager
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Async
    @EventListener
    @Transactional
    fun handle(event: StreamDetectedEvent) {
        try {
            val activeRecord = recordRepository.findByChannelIdAndPlatformTypeAndIsEndedAndIsCancelled(
                channelId = event.channelId,
                platformType = event.platformType,
                isEnded = false,
                isCancelled = false
            ) ?: return

            // 현재 서버에서 녹화 중인지 확인 (다중 서버 대비)
            if (!recordingVideoProcessManager.isProcessAlive(activeRecord.id!!)) {
                logger.debug("RecordingVideoCategoryHistoryAppendListener: Record is not running on this server: recordId={}", activeRecord.id)
                return
            }

            val currentCategory = event.stream.category
            val lastCategory = recordingVideoCategoryChangeDetectManager.getLast(activeRecord.id!!)

            // 카테고리가 변경된 경우에만 저장
            if (currentCategory != lastCategory) {
                val offsetMillis = Duration.between(activeRecord.createdAt, LocalDateTime.now()).toMillis()
                categoryHistoryAppendService.saveCategory(activeRecord.videoId, currentCategory, offsetMillis)
                recordingVideoCategoryChangeDetectManager.update(activeRecord.id!!, currentCategory)
            }
        } catch (e: Exception) {
            logger.error("RecordingVideoCategoryHistoryAppendListener: Failed to save category history from event", e)
        }
    }

}
