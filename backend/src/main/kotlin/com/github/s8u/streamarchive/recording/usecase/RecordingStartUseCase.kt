package com.github.s8u.streamarchive.recording.usecase

import com.github.s8u.streamarchive.channelplatform.repository.ChannelPlatformRepository
import com.github.s8u.streamarchive.video.enums.VideoContentPrivacy
import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.record.entity.Record
import com.github.s8u.streamarchive.recording.manager.RecordingChatCollectManager
import com.github.s8u.streamarchive.recording.manager.RecordingVideoCategoryChangeDetectManager
import com.github.s8u.streamarchive.recording.manager.RecordingVideoProcessManager
import com.github.s8u.streamarchive.recording.manager.RecordingVideoTitleChangeDetectManager
import com.github.s8u.streamarchive.recording.manager.RecordingVideoViewerChangeDetectManager
import com.github.s8u.streamarchive.record.repository.RecordRepository
import com.github.s8u.streamarchive.recording.usecase.dto.command.RecordingStartCommand
import com.github.s8u.streamarchive.platform.chat.PlatformChatStrategyFactory
import com.github.s8u.streamarchive.platform.strategy.PlatformStrategyFactory
import com.github.s8u.streamarchive.video.entity.Video
import com.github.s8u.streamarchive.video.repository.VideoRepository
import com.github.s8u.streamarchive.video.service.VideoCategoryHistoryAppendService
import com.github.s8u.streamarchive.video.service.VideoThumbnailSaveService
import com.github.s8u.streamarchive.video.service.VideoTitleHistoryAppendService
import com.github.s8u.streamarchive.video.service.VideoViewerHistoryAppendService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * 녹화 시작
 *
 * 동영상과 녹화 기록을 만들고 녹화 프로세스와 채팅 수집을 시작한다.
 * 수동 취소된 스트림, 이미 녹화 중인 스트림, 연속 시작 실패가 많은 스트림은 시작하지 않는다.
 */
@Service
class RecordingStartUseCase(
    private val recordRepository: RecordRepository,
    private val videoRepository: VideoRepository,
    private val recordingVideoProcessManager: RecordingVideoProcessManager,
    private val recordingChatCollectManager: RecordingChatCollectManager,
    private val platformStrategyFactory: PlatformStrategyFactory,
    private val platformChatStrategyFactory: PlatformChatStrategyFactory,
    private val channelPlatformRepository: ChannelPlatformRepository,
    private val videoThumbnailSaveService: VideoThumbnailSaveService,
    private val viewerHistoryAppendService: VideoViewerHistoryAppendService,
    private val titleHistoryAppendService: VideoTitleHistoryAppendService,
    private val categoryHistoryAppendService: VideoCategoryHistoryAppendService,
    private val recordingVideoViewerChangeDetectManager: RecordingVideoViewerChangeDetectManager,
    private val recordingVideoTitleChangeDetectManager: RecordingVideoTitleChangeDetectManager,
    private val recordingVideoCategoryChangeDetectManager: RecordingVideoCategoryChangeDetectManager
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        // 같은 스트림이 이 횟수만큼 연속 시작 실패하면 재녹화를 중단 (폭주 방지)
        private const val MAX_FAILED_ATTEMPTS = 3L
    }

    @Transactional
    fun start(command: RecordingStartCommand): Record? {
        val channelId = command.channelId
        val stream = command.stream
        val recordQuality = command.recordQuality

        // 사용자가 취소한 방송인지 체크
        val wasCancelled = recordRepository.existsByPlatformTypeAndPlatformStreamIdAndIsCancelled(
            platformType = stream.platformType,
            platformStreamId = stream.id,
            isCancelled = true
        )

        if (wasCancelled) {
            logger.debug("RecordingStartUseCase: Stream was manually cancelled, skipping: platformType={}, streamId={}", stream.platformType, stream.id)
            return null
        }

        // 중복 녹화 체크 (현재 녹화 중인지)
        val isAlreadyRecording = recordRepository.existsByPlatformTypeAndPlatformStreamIdAndIsEndedAndIsCancelled(
            platformType = stream.platformType,
            platformStreamId = stream.id,
            isEnded = false,
            isCancelled = false
        )

        if (isAlreadyRecording) {
            logger.debug("RecordingStartUseCase: Already recording stream: platformType={}, streamId={}", stream.platformType, stream.id)
            return null
        }

        // 연속 시작 실패 폭주 방지: 같은 스트림이 일정 횟수 이상 실패하면 재녹화 중단
        val failedCount = recordRepository.countByPlatformTypeAndPlatformStreamIdAndIsFailed(
            platformType = stream.platformType,
            platformStreamId = stream.id,
            isFailed = true
        )

        if (failedCount >= MAX_FAILED_ATTEMPTS) {
            logger.warn(
                "RecordingStartUseCase: Stream exceeded max failed attempts, skipping: platformType={}, streamId={}, failedCount={}",
                stream.platformType, stream.id, failedCount
            )
            return null
        }

        val strategy = platformStrategyFactory.getPlatformStrategy(stream.platformType)

        // 채팅 미지원 플랫폼이면 싱크 오프셋은 0
        val chatSyncOffsetMillis = platformChatStrategyFactory
            .findPlatformChatStrategy(stream.platformType)
            ?.chatSyncOffsetMillis ?: 0L

        // Video 생성
        val video = Video(
            uuid = UUID.randomUUID().toString(),
            channelId = channelId,
            title = stream.title ?: "Untitled",
            contentPrivacy = VideoContentPrivacy.PUBLIC,
            chatSyncOffsetMillis = chatSyncOffsetMillis
        )
        val savedVideo = videoRepository.save(video)

        // 썸네일 저장
        videoThumbnailSaveService.saveThumbnail(stream.thumbnailUrl, savedVideo.id!!)
        videoThumbnailSaveService.savePeakThumbnail(stream.thumbnailUrl, savedVideo.id!!)

        // Record 생성
        val record = Record(
            channelId = channelId,
            videoId = savedVideo.id!!,
            platformType = stream.platformType,
            platformStreamId = stream.id,
            recordQuality = recordQuality.streamlinkValue
        )
        val savedRecord = recordRepository.save(record)

        // 프로세스 시작
        try {
            val channelPlatform = channelPlatformRepository.findByChannelIdAndPlatformType(
                channelId = channelId,
                platformType = stream.platformType
            ) ?: throw BusinessException(
                "채널 플랫폼을 찾을 수 없습니다: channelId=$channelId, platformType=${stream.platformType}",
                HttpStatus.NOT_FOUND
            )

            val streamUrl = strategy.getStreamUrl(channelPlatform.platformChannelId)
            val streamlinkArgs = strategy.getStreamlinkArgs()

            recordingVideoProcessManager.startRecording(
                recordId = savedRecord.id!!,
                streamUrl = streamUrl,
                quality = recordQuality.streamlinkValue,
                videoId = savedVideo.id!!,
                streamlinkArgs = streamlinkArgs
            )

            // 초기 메타데이터 저장 (직전 값으로 기록해 이후 변경 감지의 기준으로 삼는다)
            stream.viewerCount?.let {
                viewerHistoryAppendService.saveViewerCount(savedVideo.id!!, it, 0)
                recordingVideoViewerChangeDetectManager.update(savedRecord.id!!, it)
            }
            stream.title?.let {
                titleHistoryAppendService.saveTitle(savedVideo.id!!, it, 0)
                recordingVideoTitleChangeDetectManager.update(savedRecord.id!!, it)
            }
            categoryHistoryAppendService.saveCategory(savedVideo.id!!, stream.category, 0)
            recordingVideoCategoryChangeDetectManager.update(savedRecord.id!!, stream.category)

            // 채팅 수집 시작
            recordingChatCollectManager.startCollecting(
                recordId = savedRecord.id!!,
                videoId = savedVideo.id!!,
                platformType = stream.platformType,
                platformChannelId = channelPlatform.platformChannelId,
                recordStartedAt = savedRecord.createdAt
            )

            logger.info(
                "RecordingStartUseCase: Started recording: recordId={}, videoId={}, channelId={}, platformType={}, streamId={}, quality={}",
                savedRecord.id,
                video.id,
                channelId,
                stream.platformType,
                stream.id,
                recordQuality
            )
        } catch (e: Exception) {
            // 프로세스 시작 실패 시 Record를 실패 상태로 변경 (수동 취소와 구분, 폭주 방지 카운트에 포함)
            savedRecord.failToStart()
            recordRepository.save(savedRecord)

            logger.error("RecordingStartUseCase: Failed to start recording process: recordId={}", savedRecord.id, e)
            throw BusinessException("녹화 프로세스 시작 실패: ${e.message}", HttpStatus.INTERNAL_SERVER_ERROR)
        }

        return savedRecord
    }

}
