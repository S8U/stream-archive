package com.github.s8u.streamarchive.client.twitch

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@Tag("external")
@SpringBootTest
@ActiveProfiles("test")
class TwitchApiClientTest {

    @Autowired
    private lateinit var twitchApiClient: TwitchApiClient

    private val objectMapper = ObjectMapper().apply {
        enable(SerializationFeature.INDENT_OUTPUT)
    }

    @Test
    fun testCreateOauthToken() {
        val response = twitchApiClient.createOauthToken()

        println("OAuth Token: ${response?.accessToken}")
        val actual = assertNotNull(response)
        assertTrue(actual.accessToken.isNotEmpty())
    }

    @Test
    fun testGetUsers() {
        val request = TwitchUsersRequestDto(login = listOf("nokduro"))
        val response = twitchApiClient.getUsers(request)

        println("response: ${objectMapper.writeValueAsString(response)}")

        val actual = assertNotNull(response)
        assertTrue(actual.data.isNotEmpty())
    }

    @Test
    fun testGetStreams() {
        val request = TwitchStreamsRequestDto(
            userLogin = "nokduro",
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

        assertNotNull(response)
    }

    @Test
    fun testRefreshOauthToken() {
        val newToken = twitchApiClient.refreshOauthToken()

        println("Refreshed Token: $newToken")
        assertTrue(assertNotNull(newToken).isNotEmpty())
    }
}
