package com.github.s8u.streamarchive.platform

import com.github.s8u.streamarchive.client.soop.SoopApiClient
import com.github.s8u.streamarchive.client.soop.SoopBroadDto
import com.github.s8u.streamarchive.client.soop.SoopLiveDetailDto
import com.github.s8u.streamarchive.client.soop.SoopLiveResponseDto
import com.github.s8u.streamarchive.client.soop.SoopStationDto
import com.github.s8u.streamarchive.client.soop.SoopStationResponseDto
import com.github.s8u.streamarchive.enums.PlatformType
import com.github.s8u.streamarchive.platform.impl.SoopStrategy
import io.mockk.every
import io.mockk.mockk
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test

class SoopStrategyTest {

    private val apiClient = mockk<SoopApiClient>()
    private val soopStrategy = SoopStrategy(apiClient)

    @Test
    fun `스트림 URL을 생성하면 숲 도메인과 사용자 ID를 포함한다`() {
        val streamUrl = soopStrategy.getStreamUrl(USER_ID)

        assertTrue(streamUrl.contains("play.sooplive.co.kr"))
        assertTrue(streamUrl.contains(USER_ID))
    }

    @Test
    fun `스테이션 응답을 공통 채널 DTO로 변환한다`() {
        every { apiClient.getStation(USER_ID) } returns soopStationResponse()

        val channel = soopStrategy.getChannel(USER_ID)

        val actual = assertNotNull(channel)
        assertEquals(PlatformType.SOOP, actual.platformType)
        assertEquals(USER_ID, actual.id)
        assertEquals(USER_ID, actual.username)
        assertEquals("테스트 채널", actual.name)
        assertEquals("https://example.com/profile.png", actual.thumbnailUrl)
    }

    @Test
    fun `스테이션 응답이 없으면 null을 반환한다`() {
        every { apiClient.getStation(USER_ID) } returns null

        val channel = soopStrategy.getChannel(USER_ID)

        assertNull(channel)
    }

    @Test
    fun `RESULT 1 라이브 상세 응답을 공통 스트림 DTO로 변환한다`() {
        every { apiClient.getLiveDetail(USER_ID) } returns SoopLiveResponseDto(
            channel = soopLiveDetail(result = 1)
        )
        every { apiClient.getStation(USER_ID) } returns soopStationResponse()

        val stream = soopStrategy.getStream(USER_ID)

        val actual = assertNotNull(stream)
        assertEquals(PlatformType.SOOP, actual.platformType)
        assertEquals(BROAD_NO, actual.id)
        assertEquals(USER_ID, actual.username)
        assertEquals("테스트 방송", actual.title)
        assertEquals("테스트 카테고리", actual.category)
        assertEquals(456, actual.viewerCount)
        assertEquals("https://liveimg.sooplive.co.kr/h/$BROAD_NO.webp", actual.thumbnailUrl)
        assertEquals(2026, actual.startedAt?.year)
    }

    @Test
    fun `RESULT 0 라이브 상세 응답은 null을 반환한다`() {
        every { apiClient.getLiveDetail(USER_ID) } returns SoopLiveResponseDto(
            channel = soopLiveDetail(result = 0)
        )

        val stream = soopStrategy.getStream(USER_ID)

        assertNull(stream)
    }

    @Test
    fun `BNO가 없으면 null을 반환한다`() {
        every { apiClient.getLiveDetail(USER_ID) } returns SoopLiveResponseDto(
            channel = soopLiveDetail(result = 1, bno = null)
        )

        val stream = soopStrategy.getStream(USER_ID)

        assertNull(stream)
    }

    private fun soopStationResponse(): SoopStationResponseDto {
        return SoopStationResponseDto(
            profileImage = "//example.com/profile.png",
            station = SoopStationDto(
                stationNo = 1,
                userId = USER_ID,
                userNick = "테스트 채널",
                stationName = "테스트 스테이션",
                stationTitle = "테스트 스테이션 제목",
                grade = 0,
                broadStart = "2026-05-13 20:00:00",
                totalBroadTime = 1000
            ),
            broad = SoopBroadDto(
                userId = USER_ID,
                broadNo = BROAD_NO.toLong(),
                broadCateNo = 1,
                broadTitle = "테스트 방송",
                currentSumViewer = 456,
                broadGrade = 0,
                subscriptionOnly = 0,
                isPassword = false
            )
        )
    }

    private fun soopLiveDetail(result: Int, bno: String? = BROAD_NO): SoopLiveDetailDto {
        return if (result == 1) {
            SoopLiveDetailDto(
                result = result,
                bno = bno,
                bjid = USER_ID,
                bjnick = "테스트 채널",
                title = "테스트 방송",
                cate = "테스트 카테고리",
                btime = 1000,
                resolution = "1920x1080",
                bps = "8000"
            )
        } else {
            SoopLiveDetailDto(
                result = result,
                bno = null,
                bjid = null,
                bjnick = null,
                title = null,
                cate = null,
                btime = null,
                resolution = null,
                bps = null
            )
        }
    }

    companion object {
        private const val USER_ID = "test-user-id"
        private const val BROAD_NO = "98765"
    }
}
