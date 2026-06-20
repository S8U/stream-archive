package com.github.s8u.streamarchive.recording.listener

import com.github.s8u.streamarchive.detect.event.StreamDetectedEvent
import com.github.s8u.streamarchive.recording.manager.RecordingVideoProcessManager
import com.github.s8u.streamarchive.recording.manager.RecordingVideoViewerChangeDetectManager
import com.github.s8u.streamarchive.record.repository.RecordRepository
import com.github.s8u.streamarchive.video.service.VideoThumbnailSaveService
import com.github.s8u.streamarchive.video.service.VideoViewerHistoryAppendService
import com.github.s8u.streamarchive.video.service.VideoViewerHistoryGetService
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDateTime

/**
 * 녹화 중인 스트리밍의 시청자 수를 저장하는 리스너
 *
 * 시청자 수가 변경된 경우에만 저장한다.
 * 직전 피크를 넘으면 피크 썸네일도 저장한다.
 */
@Component
class RecordingVideoViewerHistoryAppendListener(
    private val recordRepository: RecordRepository,
    private val recordingVideoProcessManager: RecordingVideoProcessManager,
    private val viewerHistoryAppendService: VideoViewerHistoryAppendService,
    private val viewerHistoryGetService: VideoViewerHistoryGetService,
    private val recordingVideoViewerChangeDetectManager: RecordingVideoViewerChangeDetectManager,
    private val videoThumbnailSaveService: VideoThumbnailSaveService
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Async
    @EventListener
    @Transactional
    fun handle(event: StreamDetectedEvent) {
        try {
            // 해당 채널+플랫폼의 녹화 중인 Record 조회
            val activeRecord = recordRepository.findByChannelIdAndPlatformTypeAndIsEndedAndIsCancelled(
                channelId = event.channelId,
                platformType = event.platformType,
                isEnded = false,
                isCancelled = false
            ) ?: return

            // 현재 서버에서 녹화 중인지 확인 (다중 서버 대비)
            if (!recordingVideoProcessManager.isProcessAlive(activeRecord.id!!)) {
                logger.debug("RecordingVideoViewerHistoryAppendListener: Record is not running on this server: recordId={}", activeRecord.id)
                return
            }

            val currentViewerCount = event.stream.viewerCount
            val lastViewerCount = recordingVideoViewerChangeDetectManager.getLast(activeRecord.id!!)

            // 시청자 수가 변경된 경우에만 저장
            if (currentViewerCount != null && currentViewerCount != lastViewerCount) {
                val previousPeakViewerCount = viewerHistoryGetService.findPeak(activeRecord.videoId)?.viewerCount
                val offsetMillis = Duration.between(activeRecord.createdAt, LocalDateTime.now()).toMillis()
                viewerHistoryAppendService.saveViewerCount(activeRecord.videoId, currentViewerCount, offsetMillis)
                recordingVideoViewerChangeDetectManager.update(activeRecord.id!!, currentViewerCount)

                if (previousPeakViewerCount == null || currentViewerCount > previousPeakViewerCount) {
                    videoThumbnailSaveService.savePeakThumbnail(event.stream.thumbnailUrl, activeRecord.videoId)
                }
            }
        } catch (e: Exception) {
            logger.error("RecordingVideoViewerHistoryAppendListener: Failed to save viewer history from event", e)
        }
    }

}
