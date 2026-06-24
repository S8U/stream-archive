package com.github.s8u.streamarchive.recording.usecase

import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.global.service.TransactionRunner
import com.github.s8u.streamarchive.recording.manager.RecordingChatCollectManager
import com.github.s8u.streamarchive.recording.manager.RecordingEndStateManager
import com.github.s8u.streamarchive.recording.manager.RecordingVideoCategoryChangeDetectManager
import com.github.s8u.streamarchive.recording.manager.RecordingVideoProcessManager
import com.github.s8u.streamarchive.recording.manager.RecordingVideoTitleChangeDetectManager
import com.github.s8u.streamarchive.recording.manager.RecordingVideoViewerChangeDetectManager
import com.github.s8u.streamarchive.record.repository.RecordRepository
import com.github.s8u.streamarchive.video.repository.VideoRepository
import com.github.s8u.streamarchive.video.service.VideoDeleteService
import com.github.s8u.streamarchive.video.service.VideoMetadataCalculateService
import com.github.s8u.streamarchive.video.service.VideoThumbnailSaveService
import com.github.s8u.streamarchive.video.service.VideoTitleHistoryGetService
import com.github.s8u.streamarchive.video.service.VideoViewerHistoryGetService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.time.Duration

/**
 * 녹화 종료
 *
 * 녹화 프로세스가 끝났거나 사용자가 취소하면 채팅 수집을 멈춘다.
 * 녹화 기록을 종료 처리한다.
 * 너무 짧은 녹화는 동영상을 지운다.
 * 그 외에는 최고 시청자 시점 메타데이터와 파일 정보를 확정한다.
 */
@Service
class RecordingEndUseCase(
    private val transactionRunner: TransactionRunner,
    private val recordRepository: RecordRepository,
    private val videoRepository: VideoRepository,
    private val recordingVideoProcessManager: RecordingVideoProcessManager,
    private val recordingChatCollectManager: RecordingChatCollectManager,
    private val videoMetadataCalculateService: VideoMetadataCalculateService,
    private val videoThumbnailSaveService: VideoThumbnailSaveService,
    private val videoDeleteService: VideoDeleteService,
    private val viewerHistoryGetService: VideoViewerHistoryGetService,
    private val titleHistoryGetService: VideoTitleHistoryGetService,
    private val recordingVideoViewerChangeDetectManager: RecordingVideoViewerChangeDetectManager,
    private val recordingVideoTitleChangeDetectManager: RecordingVideoTitleChangeDetectManager,
    private val recordingVideoCategoryChangeDetectManager: RecordingVideoCategoryChangeDetectManager,
    private val recordingEndStateManager: RecordingEndStateManager
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        // 이 시간(초) 미만으로 끝난 녹화는 시작 실패로 간주
        private const val MIN_RECORDING_DURATION_SECONDS = 10L
    }

    /**
     * 녹화를 종료한다.
     *
     * [isCancelled]가 true면 녹화 프로세스를 먼저 중지한다.
     */
    fun end(
        recordId: Long,
        isCancelled: Boolean = false
    ) {
        // 이미 처리 중이면 스킵 (중복 호출 방지)
        if (!recordingEndStateManager.markEnding(recordId)) {
            logger.debug("RecordingEndUseCase: Record already being ended by another thread: recordId={}", recordId)
            return
        }

        try {
            // 수동 취소일 경우 프로세스 강제 종료
            if (isCancelled) {
                recordingVideoProcessManager.stopRecording(recordId)
            }

            // 채팅 수집 종료
            recordingChatCollectManager.stopCollecting(recordId)

            // 메타데이터 변경 감지 상태 정리
            recordingVideoViewerChangeDetectManager.clear(recordId)
            recordingVideoTitleChangeDetectManager.clear(recordId)
            recordingVideoCategoryChangeDetectManager.clear(recordId)

            // 녹화 종료 확정 (짧은 트랜잭션으로 먼저 처리)
            val ended = endRecord(recordId, isCancelled) ?: return

            // 너무 짧게 끝난 녹화는 쓸모없으므로 동영상 삭제 (수동 취소/실패 공통)
            if (ended.durationSeconds < MIN_RECORDING_DURATION_SECONDS) {
                deleteShortRecording(recordId, ended.videoId, ended.durationSeconds, isCancelled)
                return
            }

            finalizeVideoMetadata(ended.videoId)
        } finally {
            recordingEndStateManager.unmarkEnding(recordId)
        }
    }

    /**
     * 녹화 기록을 종료 상태로 확정한다.
     *
     * 이미 종료된 녹화면 null을 반환한다.
     */
    private fun endRecord(
        recordId: Long,
        isCancelled: Boolean
    ): EndedRecording? {
        return transactionRunner.run {
            val record = recordRepository.findById(recordId).orElseThrow {
                BusinessException("녹화를 찾을 수 없습니다: $recordId", HttpStatus.NOT_FOUND)
            }

            if (record.isEnded) {
                logger.warn("RecordingEndUseCase: Record already ended: recordId={}", recordId)
                return@run null
            }

            record.end(isCancelled)
            recordRepository.save(record)

            logger.info(
                "RecordingEndUseCase: Ended recording: recordId={}, channelId={}, platformType={}, " +
                    "streamId={}, cancelled={}",
                recordId,
                record.channelId,
                record.platformType,
                record.platformStreamId,
                isCancelled
            )

            EndedRecording(
                videoId = record.videoId,
                durationSeconds = Duration.between(record.createdAt, record.endedAt).seconds
            )
        }
    }

    /**
     * 너무 짧게 끝난 녹화의 동영상을 지운다.
     *
     * 수동 취소가 아니면 시작 실패로 표시해 재녹화 폭주 방지 카운트에 포함시킨다.
     * 동영상 삭제와 실패 표시는 함께 성공하거나 함께 실패하게 둔다.
     */
    private fun deleteShortRecording(
        recordId: Long,
        videoId: Long,
        durationSeconds: Long,
        isCancelled: Boolean
    ) {
        val isFailureMarkRequired = !isCancelled
        logger.warn(
            "RecordingEndUseCase: Recording duration too short, deleting video: recordId={}, " +
                "videoId={}, duration={}s, markFailed={}",
            recordId,
            videoId,
            durationSeconds,
            isFailureMarkRequired
        )

        try {
            transactionRunner.run {
                videoDeleteService.delete(videoId)
                if (isFailureMarkRequired) {
                    val record = recordRepository.findById(recordId).orElseThrow {
                        BusinessException("녹화를 찾을 수 없습니다: $recordId", HttpStatus.NOT_FOUND)
                    }
                    record.markFailed()
                    recordRepository.save(record)
                }
            }
        } catch (e: Exception) {
            logger.error(
                "RecordingEndUseCase: Failed to delete short recording video: recordId={}, videoId={}",
                recordId,
                videoId,
                e
            )
        }
    }

    /**
     * 동영상의 대표 메타데이터와 파일 정보를 확정한다.
     *
     * 각 단계는 실패해도 녹화 종료에 영향을 주지 않도록 독립적으로 처리한다.
     */
    private fun finalizeVideoMetadata(videoId: Long) {
        // 대표 메타데이터를 시청자 수 피크 시점으로 확정
        try {
            applyPeakViewerMetadata(videoId)
        } catch (e: Exception) {
            logger.error("RecordingEndUseCase: Failed to apply peak viewer metadata: videoId={}", videoId, e)
        }

        // 파일 크기와 재생 시간을 확정 (계산은 트랜잭션 밖, 저장만 짧은 트랜잭션)
        try {
            val fileSize = videoMetadataCalculateService.calculateFileSize(videoId)
            val duration = videoMetadataCalculateService.calculateDuration(videoId)
            transactionRunner.runWithRetry {
                val video = videoRepository.findById(videoId).orElseThrow {
                    BusinessException("동영상을 찾을 수 없습니다: $videoId", HttpStatus.NOT_FOUND)
                }
                video.applyMetadata(fileSize = fileSize, duration = duration)
                videoRepository.save(video)
            }

            logger.info(
                "RecordingEndUseCase: Updated video metadata: videoId={}, fileSize={} bytes, duration={} seconds",
                videoId,
                fileSize,
                duration
            )
        } catch (e: Exception) {
            // 메타데이터 확정 실패는 녹화 종료를 막지 않음
            logger.error("RecordingEndUseCase: Failed to update video metadata: videoId={}", videoId, e)
        }
    }

    private fun applyPeakViewerMetadata(videoId: Long) {
        val peakViewerHistory = viewerHistoryGetService.findPeak(videoId) ?: return
        val peakTitle = titleHistoryGetService.findTitleAtOrBefore(videoId, peakViewerHistory.offsetMillis)

        if (peakTitle != null) {
            transactionRunner.runWithRetry {
                val video = videoRepository.findById(videoId).orElseThrow {
                    BusinessException("동영상을 찾을 수 없습니다: $videoId", HttpStatus.NOT_FOUND)
                }
                video.changeTitle(peakTitle)
                videoRepository.save(video)
            }
        }

        videoThumbnailSaveService.applyPeakThumbnail(videoId)

        logger.info(
            "RecordingEndUseCase: Applied peak viewer metadata: videoId={}, viewerCount={}, offsetMillis={}",
            videoId,
            peakViewerHistory.viewerCount,
            peakViewerHistory.offsetMillis
        )
    }

}

// 종료 확정 트랜잭션이 후속 분기에 넘기는 값
private data class EndedRecording(
    val videoId: Long,
    val durationSeconds: Long
)
