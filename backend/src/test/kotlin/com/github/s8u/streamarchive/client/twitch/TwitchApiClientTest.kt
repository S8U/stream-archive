package com.github.s8u.streamarchive.client.twitch

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

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
        assert(response?.accessToken?.isNotEmpty() == true)
    }

    @Test
    fun testGetUsers() {
        val request = TwitchUsersRequestDto(login = listOf("nokduro"))
        val response = twitchApiClient.getUsers(request)

        println("response: ${objectMapper.writeValueAsString(response)}")

        assert(response?.data?.isNotEmpty() == true)
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

        assert(response != null)
    }

    @Test
    fun testRefreshOauthToken() {
        val newToken = twitchApiClient.refreshOauthToken()

        println("Refreshed Token: $newToken")
        assert(newToken?.isNotEmpty() == true)
    }
}