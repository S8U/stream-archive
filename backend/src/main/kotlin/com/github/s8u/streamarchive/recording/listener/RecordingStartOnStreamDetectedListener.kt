package com.github.s8u.streamarchive.recording.listener

import com.github.s8u.streamarchive.detect.event.StreamDetectedEvent
import com.github.s8u.streamarchive.recordschedule.enums.RecordScheduleType
import com.github.s8u.streamarchive.recording.usecase.RecordingStartUseCase
import com.github.s8u.streamarchive.recording.usecase.dto.command.RecordingStartCommand
import com.github.s8u.streamarchive.recordschedule.repository.RecordScheduleRepository
import com.github.s8u.streamarchive.recordschedule.usecase.RecordScheduleAdminDeleteUseCase
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

/**
 * 스트리밍이 감지되면 녹화를 시작하는 리스너
 *
 * 채널과 플랫폼의 활성 스케줄 중 오늘 녹화 대상인 것을 찾아 우선순위가 가장 높은 스케줄로 녹화를 건다.
 * ONCE 스케줄은 녹화를 건 뒤 삭제한다.
 */
@Component
class RecordingStartOnStreamDetectedListener(
    private val recordScheduleRepository: RecordScheduleRepository,
    private val recordingStartUseCase: RecordingStartUseCase,
    private val recordScheduleAdminDeleteUseCase: RecordScheduleAdminDeleteUseCase
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Async
    @EventListener
    fun handle(event: StreamDetectedEvent) {
        try {
            // 해당 채널+플랫폼의 활성 스케줄 조회
            val schedules = recordScheduleRepository.findByChannelIdAndPlatformType(
                channelId = event.channelId,
                platformType = event.platformType
            )

            if (schedules.isEmpty()) {
                logger.debug(
                    "RecordingStartOnStreamDetectedListener: No active schedules found: channelId={}, platformType={}",
                    event.channelId,
                    event.platformType
                )
                return
            }

            // 오늘 녹화해야 하는 스케줄 필터링
            val todaySchedules = schedules.filter { it.scheduleType.calculateIsToday(it.value) }

            if (todaySchedules.isEmpty()) {
                logger.debug(
                    "RecordingStartOnStreamDetectedListener: No schedules match today: channelId={}, platformType={}",
                    event.channelId,
                    event.platformType
                )
                return
            }

            // 우선순위가 가장 높은 스케줄 선택 (priority가 높을수록 우선)
            val topSchedule = todaySchedules.maxByOrNull { it.priority }

            if (topSchedule != null) {
                recordingStartUseCase.start(
                    RecordingStartCommand(
                        channelId = event.channelId,
                        stream = event.stream,
                        recordQuality = topSchedule.recordQuality,
                        autoArchive = topSchedule.autoArchive
                    )
                )

                // ONCE 스케줄인 경우 녹화 시작 후 삭제
                if (topSchedule.scheduleType == RecordScheduleType.ONCE) {
                    try {
                        recordScheduleAdminDeleteUseCase.delete(topSchedule.id!!)
                        logger.info(
                            "RecordingStartOnStreamDetectedListener: Deleted ONCE schedule after recording started: scheduleId={}, channelId={}, platformType={}",
                            topSchedule.id,
                            event.channelId,
                            event.platformType
                        )
                    } catch (e: Exception) {
                        // 스케줄 삭제 실패는 녹화를 막지 않음
                        logger.error(
                            "RecordingStartOnStreamDetectedListener: Failed to delete ONCE schedule: scheduleId={}",
                            topSchedule.id,
                            e
                        )
                    }
                }
            }
        } catch (e: Exception) {
            logger.error(
                "RecordingStartOnStreamDetectedListener: Failed to handle stream detected event: channelId={}, platformType={}, streamId={}",
                event.channelId,
                event.platformType,
                event.stream.id,
                e
            )
        }
    }

}
