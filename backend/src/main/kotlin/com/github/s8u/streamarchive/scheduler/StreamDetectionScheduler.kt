package com.github.s8u.streamarchive.scheduler

import com.github.s8u.streamarchive.enums.RecordScheduleType
import com.github.s8u.streamarchive.platform.PlatformStrategyFactory
import com.github.s8u.streamarchive.repository.ChannelPlatformRepository
import com.github.s8u.streamarchive.repository.RecordScheduleRepository
import com.github.s8u.streamarchive.service.RecordService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class StreamDetectionScheduler(
    private val channelPlatformRepository: ChannelPlatformRepository,
    private val recordScheduleRepository: RecordScheduleRepository,
    private val platformStrategyFactory: PlatformStrategyFactory,
    private val recordService: RecordService
) {
    private val logger = LoggerFactory.getLogger(StreamDetectionScheduler::class.java)

    @Scheduled(fixedDelay = 10000)
    fun detectStreams() {
        logger.debug("Starting stream detection")

        // 활성화된 모든 채널 플랫폼 조회
        val channelPlatforms = channelPlatformRepository.findByIsActive(true)

        channelPlatforms.forEach { channelPlatform ->
            try {
                // 해당 채널+플랫폼의 활성 스케줄 조회
                val schedules = recordScheduleRepository.findByIsActive(true)
                    .filter { it.channelId == channelPlatform.channelId && it.platformType == channelPlatform.platformType }

                if (schedules.isEmpty()) {
                    return@forEach
                }

                // 오늘 녹화해야 하는 스케줄 필터링
                val todaySchedules = schedules.filter { schedule ->
                    shouldRecordToday(schedule.scheduleType, schedule.value)
                }

                if (todaySchedules.isEmpty()) {
                    return@forEach
                }

                // 방송 중인지 확인
                val strategy = platformStrategyFactory.getPlatformStrategy(channelPlatform.platformType)
                val stream = strategy.getStream(channelPlatform.platformChannelId)

                if (stream != null) {
                    // 우선순위가 가장 높은 스케줄 선택 (priority가 높을수록 우선)
                    val topSchedule = todaySchedules.maxByOrNull { it.priority }

                    if (topSchedule != null) {
                        recordService.startRecording(
                            channelId = channelPlatform.channelId,
                            stream = stream,
                            recordQuality = topSchedule.recordQuality
                        )
                    }
                }
            } catch (e: Exception) {
                logger.error(
                    "Failed to detect stream: channelId={}, platformType={}",
                    channelPlatform.channelId,
                    channelPlatform.platformType,
                    e
                )
            }
        }

        logger.debug("Finished stream detection")
    }

    private fun shouldRecordToday(scheduleType: RecordScheduleType, value: String): Boolean {
        return scheduleType.calculateIsToday(value)
    }
}
