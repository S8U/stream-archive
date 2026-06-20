package com.github.s8u.streamarchive.recording.scheduler

import com.github.s8u.streamarchive.recording.usecase.RecordingMetadataUpdateUseCase
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * 녹화 중인 동영상 메타데이터 갱신 스케줄러
 */
@Component
@Profile("!test")
class RecordingMetadataUpdateScheduler(
    private val recordingMetadataUpdateUseCase: RecordingMetadataUpdateUseCase
) {

    @Scheduled(fixedRate = 10000)
    fun updateActiveRecordingMetadata() {
        recordingMetadataUpdateUseCase.updateActiveRecordings()
    }

}
