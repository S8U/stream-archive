package com.github.s8u.streamarchive.recording.usecase

import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.global.service.TransactionRunner
import com.github.s8u.streamarchive.recording.manager.RecordingEndStateManager
import com.github.s8u.streamarchive.recording.manager.RecordingVideoProcessManager
import com.github.s8u.streamarchive.record.repository.RecordRepository
import com.github.s8u.streamarchive.video.repository.VideoRepository
import com.github.s8u.streamarchive.video.service.VideoMetadataCalculateService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

/**
 * 녹화 중인 동영상 메타데이터 갱신
 *
 * 현재 녹화 중인 동영상의 용량과 길이를 다시 계산해 반영한다.
 * 파일 계산은 트랜잭션 밖에서 하고 저장만 짧은 트랜잭션으로 처리해, 녹화 종료와 충돌하지 않게 한다.
 * 종료 처리 중인 녹화는 불필요한 갱신을 피하기 위해 건너뛴다.
 */
@Service
class RecordingMetadataUpdateUseCase(
    private val transactionRunner: TransactionRunner,
    private val recordingVideoProcessManager: RecordingVideoProcessManager,
    private val recordRepository: RecordRepository,
    private val videoRepository: VideoRepository,
    private val videoMetadataCalculateService: VideoMetadataCalculateService,
    private val recordingEndStateManager: RecordingEndStateManager
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun updateActiveRecordings() {
        val activeRecordIds = recordingVideoProcessManager.getActiveRecordIds()
        if (activeRecordIds.isEmpty()) {
            return
        }

        logger.debug("RecordingMetadataUpdateUseCase: Updating metadata for {} active recordings", activeRecordIds.size)

        for (recordId in activeRecordIds) {
            // 종료 중인 녹화 제외 (불필요한 갱신 방지)
            if (recordingEndStateManager.isEnding(recordId)) {
                logger.debug("RecordingMetadataUpdateUseCase: Skipping metadata update for ending record: recordId={}", recordId)
                continue
            }

            try {
                // 녹화별로 독립 처리해, 한 녹화의 실패나 지연이 다른 녹화에 번지지 않게 한다
                updateOne(recordId)
            } catch (e: Exception) {
                logger.error("RecordingMetadataUpdateUseCase: Failed to update metadata for active recording: recordId={}", recordId, e)
            }
        }
    }

    private fun updateOne(recordId: Long) {
        val record = recordRepository.findById(recordId).orElse(null) ?: return
        val videoId = record.videoId

        // 느린 파일 I/O는 트랜잭션 밖에서 계산한다
        val fileSize = videoMetadataCalculateService.calculateFileSize(videoId)
        val duration = videoMetadataCalculateService.calculateDuration(videoId)

        // 계산된 값만 짧은 트랜잭션으로 저장한다 (녹화 종료와 동시 수정 시 낙관적 락 재시도)
        transactionRunner.runWithRetry {
            val video = videoRepository.findById(videoId).orElseThrow {
                BusinessException("동영상을 찾을 수 없습니다: $videoId", HttpStatus.NOT_FOUND)
            }
            video.applyMetadata(fileSize = fileSize, duration = duration)
            videoRepository.save(video)
        }

        logger.debug(
            "RecordingMetadataUpdateUseCase: Updated metadata for active recording: recordId={}, videoId={}, fileSize={}, duration={}",
            recordId,
            videoId,
            fileSize,
            duration
        )
    }

}
