package com.github.s8u.streamarchive.recording.listener

import com.github.s8u.streamarchive.detect.event.StreamDetectedEvent
import com.github.s8u.streamarchive.recording.manager.RecordingVideoProcessManager
import com.github.s8u.streamarchive.recording.manager.RecordingVideoTitleChangeDetectManager
import com.github.s8u.streamarchive.record.repository.RecordRepository
import com.github.s8u.streamarchive.video.service.VideoTitleHistoryAppendService
import com.github.s8u.streamarchive.video.service.VideoTitleUpdateService
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDateTime

/**
 * 녹화 중인 스트리밍의 제목을 저장하는 리스너
 *
 * 제목이 변경된 경우에만 저장하고 동영상 제목도 함께 갱신한다.
 */
@Component
class RecordingVideoTitleHistoryAppendListener(
    private val recordRepository: RecordRepository,
    private val recordingVideoProcessManager: RecordingVideoProcessManager,
    private val titleHistoryAppendService: VideoTitleHistoryAppendService,
    private val recordingVideoTitleChangeDetectManager: RecordingVideoTitleChangeDetectManager,
    private val titleUpdateService: VideoTitleUpdateService
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

            // 현재 서버에서 녹화 중인지 확인
            if (!recordingVideoProcessManager.isProcessAlive(activeRecord.id!!)) {
                logger.debug("RecordingVideoTitleHistoryAppendListener: Record is not running on this server: recordId={}", activeRecord.id)
                return
            }

            val currentTitle = event.stream.title
            val lastTitle = recordingVideoTitleChangeDetectManager.getLast(activeRecord.id!!)

            // 제목이 변경된 경우에만 저장
            if (currentTitle != null && currentTitle != lastTitle) {
                val offsetMillis = Duration.between(activeRecord.createdAt, LocalDateTime.now()).toMillis()
                titleHistoryAppendService.saveTitle(activeRecord.videoId, currentTitle, offsetMillis)
                recordingVideoTitleChangeDetectManager.update(activeRecord.id!!, currentTitle)
                titleUpdateService.update(activeRecord.videoId, currentTitle)
            }
        } catch (e: Exception) {
            logger.error("RecordingVideoTitleHistoryAppendListener: Failed to save title history from event", e)
        }
    }

}
