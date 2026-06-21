package com.github.s8u.streamarchive.video.usecase

import com.github.s8u.streamarchive.video.entity.Video
import com.github.s8u.streamarchive.video.entity.VideoMetadataCategoryHistory
import com.github.s8u.streamarchive.video.enums.VideoContentPrivacy
import com.github.s8u.streamarchive.video.repository.VideoMetadataCategoryHistoryRepository
import com.github.s8u.streamarchive.video.repository.VideoRepository
import com.github.s8u.streamarchive.video.service.VideoAccessAssertService
import com.github.s8u.streamarchive.video.service.VideoTitleHistoryGetService
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.jupiter.api.Test

class VideoChapterGetUseCaseTest {

    private val categoryHistoryRepository = mockk<VideoMetadataCategoryHistoryRepository>()
    private val videoRepository = mockk<VideoRepository>()
    private val videoTitleHistoryGetService = mockk<VideoTitleHistoryGetService>()
    private val videoAccessAssertService = mockk<VideoAccessAssertService>()
    private val videoChapterGetUseCase = VideoChapterGetUseCase(
        categoryHistoryRepository,
        videoRepository,
        videoTitleHistoryGetService,
        videoAccessAssertService
    )

    @Test
    fun `카테고리 변경 이력이 없으면 빈 목록을 반환한다`() {
        givenAccessibleVideo()
        every { categoryHistoryRepository.findByVideoIdOrderByOffsetMillisAsc(VIDEO_ID) } returns emptyList()

        val result = videoChapterGetUseCase.getByVideoUuid(UUID)

        assertEquals(emptyList(), result)
    }

    @Test
    fun `첫 챕터는 오프셋이 0보다 뒤에 잡혀도 0으로 당긴다`() {
        givenAccessibleVideo()
        every { categoryHistoryRepository.findByVideoIdOrderByOffsetMillisAsc(VIDEO_ID) } returns listOf(
            categoryHistory(category = "just chatting", offsetMillis = 30_000)
        )
        every { videoTitleHistoryGetService.findTitleAtOrBefore(VIDEO_ID, 0) } returns "방송 시작"

        val result = videoChapterGetUseCase.getByVideoUuid(UUID)

        assertEquals(1, result.size)
        assertEquals(0L, result.single().offsetMillis)
        assertEquals("just chatting", result.single().category)
        assertEquals("방송 시작", result.single().title)
    }

    @Test
    fun `여러 챕터는 각자 시작 시점의 제목을 매칭하고 첫 챕터만 0으로 당긴다`() {
        givenAccessibleVideo()
        every { categoryHistoryRepository.findByVideoIdOrderByOffsetMillisAsc(VIDEO_ID) } returns listOf(
            categoryHistory(category = "just chatting", offsetMillis = 5_000),
            categoryHistory(category = "발로란트", offsetMillis = 750_000),
            categoryHistory(category = "롤", offsetMillis = 6_300_000)
        )
        every { videoTitleHistoryGetService.findTitleAtOrBefore(VIDEO_ID, 0) } returns "잠깐만요"
        every { videoTitleHistoryGetService.findTitleAtOrBefore(VIDEO_ID, 750_000) } returns "발로 랭크"
        every { videoTitleHistoryGetService.findTitleAtOrBefore(VIDEO_ID, 6_300_000) } returns null

        val result = videoChapterGetUseCase.getByVideoUuid(UUID)

        assertEquals(listOf(0L, 750_000L, 6_300_000L), result.map { it.offsetMillis })
        assertEquals(listOf("just chatting", "발로란트", "롤"), result.map { it.category })
        assertEquals("발로 랭크", result[1].title)
        assertNull(result[2].title)
    }

    private fun givenAccessibleVideo() {
        val video = mockk<Video>()
        every { video.id } returns VIDEO_ID
        every { video.contentPrivacy } returns VideoContentPrivacy.PUBLIC
        every { video.channel } returns null
        every { videoRepository.findByUuid(UUID) } returns video
        every { videoAccessAssertService.assertAccessible(any(), any()) } just runs
    }

    private fun categoryHistory(
        category: String?,
        offsetMillis: Long
    ): VideoMetadataCategoryHistory {
        val history = mockk<VideoMetadataCategoryHistory>()
        every { history.category } returns category
        every { history.offsetMillis } returns offsetMillis
        return history
    }

    companion object {
        private const val UUID = "video-uuid"
        private const val VIDEO_ID = 10L
    }

}
