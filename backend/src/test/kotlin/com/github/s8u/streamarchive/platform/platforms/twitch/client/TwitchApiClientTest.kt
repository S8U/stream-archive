package com.github.s8u.streamarchive.platform.platforms.twitch.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.github.s8u.streamarchive.platform.platforms.twitch.properties.TwitchProperties
import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("external")
class TwitchApiClientTest {

    private val twitchApiClient = TwitchApiClient(twitchProperties())

    private val objectMapper = ObjectMapper().apply {
        enable(SerializationFeature.INDENT_OUTPUT)
    }

    @Tag("oauth")
    @Test
    fun `OAuth 토큰을 발급한다`() {
        val response = twitchApiClient.createOauthToken()

        val actual = assertNotNull(response)
        assertTrue(actual.accessToken.isNotEmpty())
        assertTrue(actual.expiresIn > 0)
        assertEquals("bearer", actual.tokenType)
    }

    @Test
    fun `유저를 조회하면 유저 정보를 반환한다`() {
        val request = TwitchUsersRequestDto(login = listOf(LOGIN))
        val response = twitchApiClient.getUsers(request)

        println("response: ${objectMapper.writeValueAsString(response)}")

        val actual = assertNotNull(response)
        assertEquals(1, actual.data.size)
        val user = actual.data.first()
        assertEquals(LOGIN, user.login)
        assertFalse(user.id.isBlank())
        assertFalse(user.displayName.isBlank())
    }

    @Test
    fun `스트림을 조회하면 방송 상태와 무관하게 응답을 파싱할 수 있다`() {
        val request = TwitchStreamsRequestDto(
            userLogin = LOGIN,
            userId = null,
            gameId = null,
            type = null,
            language = null,
            first = 1,
            before = null,
            after = null
        )
        val response = twitchApiClient.getStreams(request)

        println("response: ${objectMapper.writeValueAsString(response)}")

        val actual = assertNotNull(response)

        if (actual.data.isNotEmpty()) {
            val stream = actual.data.first()
            assertEquals(LOGIN, stream.userLogin)
            assertFalse(stream.id.isBlank())
            assertFalse(stream.userId.isNullOrBlank())
            assertEquals("live", stream.type)
            assertFalse(stream.title.isNullOrBlank())
            assertNotNull(stream.viewerCount)
        }
    }

    @Tag("oauth")
    @Test
    fun `OAuth 토큰을 갱신한다`() {
        val newToken = twitchApiClient.refreshOauthToken()

        assertTrue(assertNotNull(newToken).isNotEmpty())
    }

    companion object {
        private const val LOGIN = "nokduro"
        private const val TWITCH_APP_CLIENT_ID = "TWITCH_APP_CLIENT_ID"
        private const val TWITCH_APP_CLIENT_SECRET = "TWITCH_APP_CLIENT_SECRET"
        private const val TWITCH_PERSONAL_OAUTH_TOKEN = "TWITCH_PERSONAL_OAUTH_TOKEN"

        private fun twitchProperties(): TwitchProperties {
            val env = loadDotenv()

            return TwitchProperties(
                appClientId = System.getenv(TWITCH_APP_CLIENT_ID) ?: env.getProperty(TWITCH_APP_CLIENT_ID, ""),
                appClientSecret = System.getenv(TWITCH_APP_CLIENT_SECRET) ?: env.getProperty(TWITCH_APP_CLIENT_SECRET, ""),
                personalOauthToken = System.getenv(TWITCH_PERSONAL_OAUTH_TOKEN)
                    ?: env.getProperty(TWITCH_PERSONAL_OAUTH_TOKEN, "")
            )
        }

        private fun loadDotenv(): Properties {
            val properties = Properties()
            val dotenvPath = Path.of(".env")

            if (Files.exists(dotenvPath)) {
                Files.newBufferedReader(dotenvPath).use { properties.load(it) }
            }

            return properties
        }
    }
}
