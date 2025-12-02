package com.github.s8u.streamarchive.service

import com.github.s8u.streamarchive.dto.AdminRecordResponse
import com.github.s8u.streamarchive.dto.AdminRecordSearchRequest
import com.github.s8u.streamarchive.entity.Record
import com.github.s8u.streamarchive.entity.Video
import com.github.s8u.streamarchive.enums.ContentPrivacy
import com.github.s8u.streamarchive.enums.RecordQuality
import com.github.s8u.streamarchive.event.StreamDetectedEvent
import com.github.s8u.streamarchive.exception.BusinessException
import com.github.s8u.streamarchive.platform.PlatformStreamDto
import com.github.s8u.streamarchive.platform.PlatformStrategyFactory
import com.github.s8u.streamarchive.recorder.RecordProcessManager
import com.github.s8u.streamarchive.repository.ChannelPlatformRepository
import com.github.s8u.streamarchive.repository.RecordRepository
import com.github.s8u.streamarchive.repository.RecordScheduleRepository
import com.github.s8u.streamarchive.repository.VideoRepository
import com.github.s8u.streamarchive.util.UrlBuilder
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Service
class RecordService(
    private val recordRepository: RecordRepository,
    private val videoRepository: VideoRepository,
    private val recordProcessManager: RecordProcessManager,
    private val platformStrategyFactory: PlatformStrategyFactory,
    private val channelPlatformRepository: ChannelPlatformRepository,
    private val videoMetadataService: VideoMetadataService,
    private val recordScheduleRepository: RecordScheduleRepository,
    private val videoThumbnailService: VideoThumbnailService,
    private val urlBuilder: UrlBuilder
) {
    private val logger = LoggerFactory.getLogger(RecordService::class.java)
    private val endingRecords = ConcurrentHashMap.newKeySet<Long>() // 종료 처리 중인 recordId

    @Transactional(readOnly = true)
    fun searchForAdmin(request: AdminRecordSearchRequest, pageable: Pageable): Page<AdminRecordResponse> {
        return recordRepository.searchForAdmin(request, pageable)
            .map { record ->
                val channelProfileUrl = urlBuilder.channelProfileUrl(record.channel?.uuid!!)
                val videoThumbnailUrl = urlBuilder.videoThumbnailUrl(record.video?.uuid!!)
                AdminRecordResponse.from(record, channelProfileUrl, videoThumbnailUrl)
            }
    }

    @Transactional(readOnly = true)
    fun getForAdmin(id: Long): AdminRecordResponse {
        val record = recordRepository.findById(id).orElseThrow {
            BusinessException("녹화를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }
        val channelProfileUrl = urlBuilder.channelProfileUrl(record.channel?.uuid!!)
        val videoThumbnailUrl = urlBuilder.videoThumbnailUrl(record.video?.uuid!!)
        return AdminRecordResponse.from(record, channelProfileUrl, videoThumbnailUrl)
    }

    @Transactional
    fun startRecording(
        channelId: Long,
        stream: PlatformStreamDto,
        recordQuality: RecordQuality
    ): Record? {
        // 사용자가 취소한 방송인지 체크
        val wasCancelled = recordRepository.existsByPlatformTypeAndPlatformStreamIdAndIsCancelled(
            platformType = stream.platformType,
            platformStreamId = stream.id,
            isCancelled = true
        )

        if (wasCancelled) {
            logger.debug("Stream was manually cancelled, skipping: platformType={}, streamId={}", stream.platformType, stream.id)
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
            logger.debug("Already recording stream: platformType={}, streamId={}", stream.platformType, stream.id)
            return null
        }

        // Video 생성
        val video = Video(
            uuid = UUID.randomUUID().toString(),
            channelId = channelId,
            title = stream.title ?: "Untitled",
            contentPrivacy = ContentPrivacy.PUBLIC
        )
        val savedVideo = videoRepository.save(video)

        // 썸네일 저장
        videoThumbnailService.saveThumbnail(stream.thumbnailUrl, savedVideo.id!!)

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

            val strategy = platformStrategyFactory.getPlatformStrategy(stream.platformType)
            val streamUrl = strategy.getStreamUrl(channelPlatform.platformChannelId)
            val streamHeaders = strategy.getStreamHeaders()

            recordProcessManager.startRecording(
                recordId = savedRecord.id!!,
                streamUrl = streamUrl,
                quality = recordQuality.streamlinkValue,
                videoId = savedVideo.id!!,
                platformHeaders = streamHeaders
            )

            logger.info(
                "Started recording: recordId={}, videoId={}, channelId={}, platformType={}, streamId={}, quality={}",
                savedRecord.id,
                video.id,
                channelId,
                stream.platformType,
                stream.id,
                recordQuality
            )
        } catch (e: Exception) {
            // 프로세스 시작 실패 시 Record를 취소 상태로 변경
            savedRecord.isEnded = true
            savedRecord.isCancelled = true
            savedRecord.endedAt = LocalDateTime.now()
            recordRepository.save(savedRecord)

            logger.error("Failed to start recording process: recordId={}", savedRecord.id, e)
            throw BusinessException("녹화 프로세스 시작 실패: ${e.message}", HttpStatus.INTERNAL_SERVER_ERROR)
        }

        return savedRecord
    }

    @Transactional
    fun endRecording(recordId: Long, isCancel: Boolean = false) {
        // 중복 호출 방지: 이미 처리 중이면 스킵
        if (!endingRecords.add(recordId)) {
            logger.debug("Record already being ended by another thread: recordId={}", recordId)
            return
        }

        try {
            val record = recordRepository.findById(recordId).orElseThrow {
                BusinessException("녹화를 찾을 수 없습니다: $recordId", HttpStatus.NOT_FOUND)
            }

            // 이미 종료된 녹화인지 확인
            if (record.isEnded) {
                logger.warn("Record already ended: recordId={}", recordId)
                return
            }

            // 수동 취소일 경우 프로세스 강제 종료
            if (isCancel) {
                recordProcessManager.stopRecording(recordId)
            }

            // DB 업데이트
            record.isEnded = true
            record.isCancelled = isCancel
            record.endedAt = LocalDateTime.now()
            recordRepository.save(record)

            logger.info(
                "Ended recording: recordId={}, channelId={}, platformType={}, streamId={}, cancelled={}",
                recordId,
                record.channelId,
                record.platformType,
                record.platformStreamId,
                isCancel
            )

            // Video 메타데이터 업데이트
            try {
                val video = videoRepository.findById(record.videoId).orElseThrow {
                    BusinessException("동영상을 찾을 수 없습니다: ${record.videoId}", HttpStatus.NOT_FOUND)
                }

                video.fileSize = videoMetadataService.calculateFileSize(video.id!!)
                video.duration = videoMetadataService.calculateDuration(video.id!!)
                videoRepository.save(video)

                logger.info(
                    "Updated video metadata: videoId={}, fileSize={} bytes, duration={} seconds",
                    video.id,
                    video.fileSize,
                    video.duration
                )
            } catch (e: Exception) {
                // 메타데이터 업데이트 실패는 녹화 종료를 막지 않음
                logger.error("Failed to update video metadata: recordId={}", recordId, e)
            }
        } finally {
            endingRecords.remove(recordId)
        }
    }

    @Async
    @EventListener
    @Transactional
    fun handleStreamDetected(event: StreamDetectedEvent) {
        try {
            // 해당 채널+플랫폼의 활성 스케줄 조회
            val schedules = recordScheduleRepository.findByChannelIdAndPlatformType(
                channelId = event.channelPlatform?.channel?.id!!,
                platformType = event.channelPlatform.platformType
            )

            if (schedules.isEmpty()) {
                logger.debug(
                    "No active schedules found: channelId={}, platformType={}",
                    event.channelPlatform?.channel?.id!!,
                    event.channelPlatform.platformType
                )
                return
            }

            // 오늘 녹화해야 하는 스케줄 필터링
            val todaySchedules = schedules.filter { it.scheduleType.calculateIsToday(it.value) }

            if (todaySchedules.isEmpty()) {
                logger.debug(
                    "No schedules match today: channelId={}, platformType={}",
                    event.channelPlatform?.channel?.id,
                    event.channelPlatform.platformType
                )
                return
            }

            // 우선순위가 가장 높은 스케줄 선택 (priority가 높을수록 우선)
            val topSchedule = todaySchedules.maxByOrNull { it.priority }

            if (topSchedule != null) {
                startRecording(
                    channelId = event.channelPlatform?.channel?.id!!,
                    stream = event.stream,
                    recordQuality = topSchedule.recordQuality
                )
            }
        } catch (e: Exception) {
            logger.error(
                "Failed to handle stream detected event: channelId={}, platformType={}, streamId={}",
                event.channelPlatform?.channel?.id,
                event.channelPlatform.platformType,
                event.stream.id,
                e
            )
        }
    }

}