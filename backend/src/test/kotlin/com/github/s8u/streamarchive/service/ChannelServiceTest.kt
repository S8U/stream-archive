package com.github.s8u.streamarchive.service

import com.github.s8u.streamarchive.dto.AdminChannelCreateRequest
import com.github.s8u.streamarchive.dto.AdminChannelUpdateRequest
import com.github.s8u.streamarchive.entity.Channel
import com.github.s8u.streamarchive.enums.ContentPrivacy
import com.github.s8u.streamarchive.exception.BusinessException
import com.github.s8u.streamarchive.properties.StorageProperties
import com.github.s8u.streamarchive.repository.ChannelPlatformRepository
import com.github.s8u.streamarchive.repository.ChannelRepository
import com.github.s8u.streamarchive.repository.VideoRepository
import com.github.s8u.streamarchive.util.UrlBuilder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.http.HttpStatus
import java.util.*

@ExtendWith(MockitoExtension::class)
class ChannelServiceTest {

    @Mock lateinit var channelRepository: ChannelRepository
    @Mock lateinit var channelPlatformRepository: ChannelPlatformRepository
    @Mock lateinit var videoRepository: VideoRepository
    @Mock lateinit var channelPlatformService: ChannelPlatformService
    @Mock lateinit var channelProfileService: ChannelProfileService
    @Mock lateinit var recordScheduleService: RecordScheduleService
    @Mock lateinit var authenticationService: AuthenticationService
    @Mock lateinit var videoService: VideoService
    @Mock lateinit var storageProperties: StorageProperties
    @Mock lateinit var urlBuilder: UrlBuilder

    @InjectMocks
    lateinit var channelService: ChannelService

    private lateinit var testChannel: Channel

    @BeforeEach
    fun setUp() {
        testChannel = Channel(
            id = 1L,
            uuid = "channel-uuid",
            name = "Test Channel",
            contentPrivacy = ContentPrivacy.PUBLIC
        )
    }

    @Nested
    @DisplayName("getForAdmin")
    inner class GetForAdmin {

        @Test
        @DisplayName("ID로 채널을 조회한다")
        fun getChannelById() {
            whenever(channelRepository.findById(1L)).thenReturn(Optional.of(testChannel))
            whenever(urlBuilder.channelProfileUrl("channel-uuid")).thenReturn("http://api/channels/channel-uuid/profile")

            val response = channelService.getForAdmin(1L)

            assertEquals(1L, response.id)
            assertEquals("Test Channel", response.name)
        }

        @Test
        @DisplayName("존재하지 않는 채널 조회 시 예외가 발생한다")
        fun getNonExistentChannel() {
            whenever(channelRepository.findById(999L)).thenReturn(Optional.empty())

            val exception = assertThrows(BusinessException::class.java) {
                channelService.getForAdmin(999L)
            }
            assertEquals(HttpStatus.NOT_FOUND, exception.status)
        }
    }

    @Nested
    @DisplayName("getByUuidForPublic")
    inner class GetByUuidForPublic {

        @Test
        @DisplayName("공개 채널을 UUID로 조회한다")
        fun getPublicChannel() {
            whenever(channelRepository.findByUuid("channel-uuid")).thenReturn(testChannel)
            whenever(urlBuilder.channelProfileUrl("channel-uuid")).thenReturn("http://api/channels/channel-uuid/profile")

            val response = channelService.getByUuidForPublic("channel-uuid")

            assertEquals("Test Channel", response.name)
        }

        @Test
        @DisplayName("비공개 채널을 일반 사용자가 조회 시 예외가 발생한다")
        fun getPrivateChannelAsUser() {
            val privateChannel = Channel(
                id = 2L,
                uuid = "private-uuid",
                name = "Private Channel",
                contentPrivacy = ContentPrivacy.PRIVATE
            )
            whenever(channelRepository.findByUuid("private-uuid")).thenReturn(privateChannel)
            whenever(authenticationService.isAdmin()).thenReturn(false)

            val exception = assertThrows(BusinessException::class.java) {
                channelService.getByUuidForPublic("private-uuid")
            }
            assertEquals(HttpStatus.NOT_FOUND, exception.status)
        }

        @Test
        @DisplayName("비공개 채널을 관리자가 조회하면 성공한다")
        fun getPrivateChannelAsAdmin() {
            val privateChannel = Channel(
                id = 2L,
                uuid = "private-uuid",
                name = "Private Channel",
                contentPrivacy = ContentPrivacy.PRIVATE
            )
            whenever(channelRepository.findByUuid("private-uuid")).thenReturn(privateChannel)
            whenever(authenticationService.isAdmin()).thenReturn(true)
            whenever(urlBuilder.channelProfileUrl("private-uuid")).thenReturn("http://api/channels/private-uuid/profile")

            val response = channelService.getByUuidForPublic("private-uuid")

            assertEquals("Private Channel", response.name)
        }

        @Test
        @DisplayName("UNLISTED 채널은 일반 사용자도 조회할 수 있다")
        fun getUnlistedChannelAsUser() {
            val unlistedChannel = Channel(
                id = 3L,
                uuid = "unlisted-uuid",
                name = "Unlisted Channel",
                contentPrivacy = ContentPrivacy.UNLISTED
            )
            whenever(channelRepository.findByUuid("unlisted-uuid")).thenReturn(unlistedChannel)
            whenever(urlBuilder.channelProfileUrl("unlisted-uuid")).thenReturn("http://api/channels/unlisted-uuid/profile")

            val response = channelService.getByUuidForPublic("unlisted-uuid")

            assertEquals("Unlisted Channel", response.name)
        }

        @Test
        @DisplayName("존재하지 않는 UUID로 조회 시 예외가 발생한다")
        fun getNonExistentUuid() {
            whenever(channelRepository.findByUuid("nonexistent")).thenReturn(null)

            assertThrows(BusinessException::class.java) {
                channelService.getByUuidForPublic("nonexistent")
            }
        }
    }

    @Nested
    @DisplayName("createForAdmin")
    inner class CreateForAdmin {

        @Test
        @DisplayName("새 채널을 생성한다")
        fun createChannel() {
            val request = AdminChannelCreateRequest(name = "New Channel", contentPrivacy = ContentPrivacy.PUBLIC)
            whenever(channelRepository.save(any<Channel>())).thenAnswer { invocation ->
                val channel = invocation.arguments[0] as Channel
                Channel(
                    id = 10L,
                    uuid = channel.uuid,
                    name = channel.name,
                    contentPrivacy = channel.contentPrivacy
                )
            }
            whenever(urlBuilder.channelProfileUrl(any())).thenReturn("http://api/channels/uuid/profile")

            val response = channelService.createForAdmin(request)

            assertEquals("New Channel", response.name)
            assertEquals(ContentPrivacy.PUBLIC, response.contentPrivacy)
        }
    }

    @Nested
    @DisplayName("updateForAdmin")
    inner class UpdateForAdmin {

        @Test
        @DisplayName("채널 이름과 공개 범위를 수정한다")
        fun updateChannel() {
            val request = AdminChannelUpdateRequest(name = "Updated", contentPrivacy = ContentPrivacy.PRIVATE)
            whenever(channelRepository.findById(1L)).thenReturn(Optional.of(testChannel))
            whenever(urlBuilder.channelProfileUrl("channel-uuid")).thenReturn("http://api/channels/channel-uuid/profile")

            val response = channelService.updateForAdmin(1L, request)

            assertEquals("Updated", response.name)
            assertEquals(ContentPrivacy.PRIVATE, response.contentPrivacy)
        }

        @Test
        @DisplayName("null 값은 기존 값을 유지한다")
        fun updateWithNulls() {
            val request = AdminChannelUpdateRequest(name = null, contentPrivacy = null)
            whenever(channelRepository.findById(1L)).thenReturn(Optional.of(testChannel))
            whenever(urlBuilder.channelProfileUrl("channel-uuid")).thenReturn("http://api/channels/channel-uuid/profile")

            val response = channelService.updateForAdmin(1L, request)

            assertEquals("Test Channel", response.name)
            assertEquals(ContentPrivacy.PUBLIC, response.contentPrivacy)
        }
    }

    @Nested
    @DisplayName("delete")
    inner class Delete {

        @Test
        @DisplayName("채널 삭제 시 관련 엔티티도 모두 삭제한다")
        fun deleteChannelWithCascade() {
            whenever(channelRepository.findById(1L)).thenReturn(Optional.of(testChannel))

            channelService.delete(1L)

            verify(recordScheduleService).deleteAllByChannelId(1L)
            verify(channelPlatformService).deleteAllByChannelId(1L)
            verify(channelProfileService).deleteProfile(1L)
            verify(videoService).deleteAllByChannelId(1L)
            assertFalse(testChannel.isActive)
            assertNotNull(testChannel.deletedAt)
        }

        @Test
        @DisplayName("존재하지 않는 채널 삭제 시 예외가 발생한다")
        fun deleteNonExistentChannel() {
            whenever(channelRepository.findById(999L)).thenReturn(Optional.empty())

            assertThrows(BusinessException::class.java) {
                channelService.delete(999L)
            }
        }
    }

    @Nested
    @DisplayName("getStatsByUuid")
    inner class GetStatsByUuid {

        @Test
        @DisplayName("채널 통계를 조회한다")
        fun getStats() {
            whenever(channelRepository.findByUuid("channel-uuid")).thenReturn(testChannel)
            whenever(videoRepository.countByChannelId(1L)).thenReturn(5L)
            whenever(videoRepository.sumFileSizeByChannelId(1L)).thenReturn(1024000L)

            val response = channelService.getStatsByUuid("channel-uuid")

            assertEquals(5L, response.videoCount)
            assertEquals(1024000L, response.totalFileSize)
        }

        @Test
        @DisplayName("비공개 채널 통계를 일반 사용자가 조회 시 예외가 발생한다")
        fun getStatsPrivateChannelAsUser() {
            val privateChannel = Channel(
                id = 2L,
                uuid = "private-uuid",
                name = "Private",
                contentPrivacy = ContentPrivacy.PRIVATE
            )
            whenever(channelRepository.findByUuid("private-uuid")).thenReturn(privateChannel)
            whenever(authenticationService.isAdmin()).thenReturn(false)

            assertThrows(BusinessException::class.java) {
                channelService.getStatsByUuid("private-uuid")
            }
        }
    }
}
