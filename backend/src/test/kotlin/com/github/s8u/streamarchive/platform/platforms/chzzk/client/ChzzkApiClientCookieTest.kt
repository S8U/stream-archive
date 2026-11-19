package com.github.s8u.streamarchive.platform.platforms.chzzk.client

import com.github.s8u.streamarchive.platform.platforms.chzzk.properties.ChzzkProperties
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.client.ClientHttpRequestFactory
import org.springframework.mock.http.client.MockClientHttpRequest
import org.springframework.mock.http.client.MockClientHttpResponse

/**
 * 치지직 API 클라이언트 로그인 쿠키 테스트
 *
 * 실제로 요청을 보내지 않고, 클라이언트가 만든 요청의 헤더만 확인한다.
 */
class ChzzkApiClientCookieTest {

    private val requests = mutableListOf<MockClientHttpRequest>()

    // 요청을 기록하고 빈 성공 응답을 돌려준다
    private val requestFactory = ClientHttpRequestFactory { uri, method ->
        val response = MockClientHttpResponse(EMPTY_RESPONSE_BODY.toByteArray(), HttpStatus.OK)
        response.headers.contentType = MediaType.APPLICATION_JSON

        val request = MockClientHttpRequest(method, uri)
        request.setResponse(response)
        requests.add(request)
        request
    }

    @Test
    fun `로그인 쿠키가 있으면 요청에 쿠키 헤더를 붙인다`() {
        val apiClient = ChzzkApiClient(requestFactory, ChzzkProperties(nidAut = "aut-value", nidSes = "ses-value"))

        apiClient.getLiveDetail(CHANNEL_ID)

        assertEquals("NID_AUT=aut-value; NID_SES=ses-value", requests.single().headers.getFirst(HttpHeaders.COOKIE))
    }

    @Test
    fun `로그인 쿠키가 없으면 쿠키 헤더를 붙이지 않는다`() {
        val apiClient = ChzzkApiClient(requestFactory, ChzzkProperties(nidAut = "", nidSes = ""))

        apiClient.getLiveDetail(CHANNEL_ID)

        assertNull(requests.single().headers.getFirst(HttpHeaders.COOKIE))
    }

    companion object {
        private const val CHANNEL_ID = "abcdef0123456789abcdef0123456789"
        private const val EMPTY_RESPONSE_BODY = """{"code":200,"message":null,"content":null}"""
    }

}
