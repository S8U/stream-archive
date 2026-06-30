package com.github.s8u.streamarchive.video.usecase

import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.video.repository.VideoChatHistoryRepository
import com.github.s8u.streamarchive.video.repository.VideoRepository
import com.github.s8u.streamarchive.video.service.VideoAccessAssertService
import com.github.s8u.streamarchive.video.service.VideoChatAnalyzeService
import com.github.s8u.streamarchive.video.usecase.dto.result.VideoChatAnalysisGetResult
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 동영상 채팅 분석 조회 (공개)
 *
 * 구간별 채팅 개수와 대표 키워드를 함께 돌려준다.
 */
@Service
class VideoChatAnalysisGetUseCase(
    private val videoChatHistoryRepository: VideoChatHistoryRepository,
    private val videoRepository: VideoRepository,
    private val videoAccessAssertService: VideoAccessAssertService,
    private val videoChatAnalyzeService: VideoChatAnalyzeService
) {

    @Transactional(readOnly = true)
    fun getByVideoUuid(
        uuid: String,
        bucketSeconds: Long,
        keywordCount: Int
    ): VideoChatAnalysisGetResult {
        if (bucketSeconds < MIN_BUCKET_SECONDS || bucketSeconds > MAX_BUCKET_SECONDS) {
            throw BusinessException(
                "버킷 단위는 ${MIN_BUCKET_SECONDS}초 이상 ${MAX_BUCKET_SECONDS}초 이하여야 합니다.",
                HttpStatus.BAD_REQUEST
            )
        }

        val video = videoRepository.findByUuid(uuid)
            ?: throw BusinessException("동영상을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)

        videoAccessAssertService.assertAccessible(video.contentPrivacy, video.channel?.contentPrivacy)

        val bucketMillis = bucketSeconds * MILLIS_PER_SECOND

        // 전 구간 분석을 위해 동영상의 전체 채팅을 한 번에 조회한다(오프셋·원문만 투영).
        val messages = videoChatHistoryRepository.findMessagesByVideoId(video.id!!)

        val analyzeResult = videoChatAnalyzeService.analyze(
            messages = messages,
            bucketMillis = bucketMillis,
            keywordCount = keywordCount,
            platformType = video.record?.platformType
        )

        return VideoChatAnalysisGetResult.from(analyzeResult)
    }

    companion object {
        private const val MILLIS_PER_SECOND = 1000L
        private const val MIN_BUCKET_SECONDS = 10L
        private const val MAX_BUCKET_SECONDS = 600L
    }

}
