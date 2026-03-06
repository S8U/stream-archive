package com.github.s8u.streamarchive.exception

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class GlobalExceptionHandlerTest {

    private val handler = GlobalExceptionHandler()

    @Nested
    @DisplayName("handleBusinessException")
    inner class HandleBusinessException {

        @Test
        @DisplayName("BusinessException을 적절한 상태 코드와 메시지로 처리한다")
        fun handleBadRequest() {
            val exception = BusinessException("잘못된 요청입니다.", HttpStatus.BAD_REQUEST)

            val response = handler.handleBusinessException(exception)

            assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
            assertEquals("잘못된 요청입니다.", response.body?.message)
            assertEquals(400, response.body?.status)
        }

        @Test
        @DisplayName("NOT_FOUND 상태의 BusinessException을 처리한다")
        fun handleNotFound() {
            val exception = BusinessException("리소스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)

            val response = handler.handleBusinessException(exception)

            assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
            assertEquals("리소스를 찾을 수 없습니다.", response.body?.message)
            assertEquals(404, response.body?.status)
        }

        @Test
        @DisplayName("UNAUTHORIZED 상태의 BusinessException을 처리한다")
        fun handleUnauthorized() {
            val exception = BusinessException("인증이 필요합니다.", HttpStatus.UNAUTHORIZED)

            val response = handler.handleBusinessException(exception)

            assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
            assertEquals(401, response.body?.status)
        }

        @Test
        @DisplayName("CONFLICT 상태의 BusinessException을 처리한다")
        fun handleConflict() {
            val exception = BusinessException("이미 존재합니다.", HttpStatus.CONFLICT)

            val response = handler.handleBusinessException(exception)

            assertEquals(HttpStatus.CONFLICT, response.statusCode)
            assertEquals(409, response.body?.status)
        }
    }

    @Nested
    @DisplayName("handleException")
    inner class HandleException {

        @Test
        @DisplayName("일반 Exception을 500 상태 코드로 처리한다")
        fun handleGenericException() {
            val exception = RuntimeException("예기치 않은 오류")

            val response = handler.handleException(exception)

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
            assertEquals("Internal server error", response.body?.message)
            assertEquals(500, response.body?.status)
        }

        @Test
        @DisplayName("NullPointerException을 500 상태 코드로 처리한다")
        fun handleNpe() {
            val exception = NullPointerException("null 참조")

            val response = handler.handleException(exception)

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        }
    }

    @Nested
    @DisplayName("BusinessException")
    inner class BusinessExceptionTest {

        @Test
        @DisplayName("기본 상태 코드는 BAD_REQUEST이다")
        fun defaultStatus() {
            val exception = BusinessException("오류")

            assertEquals(HttpStatus.BAD_REQUEST, exception.status)
            assertEquals("오류", exception.message)
        }

        @Test
        @DisplayName("커스텀 상태 코드를 설정할 수 있다")
        fun customStatus() {
            val exception = BusinessException("찾을 수 없음", HttpStatus.NOT_FOUND)

            assertEquals(HttpStatus.NOT_FOUND, exception.status)
        }

        @Test
        @DisplayName("RuntimeException을 상속한다")
        fun isRuntimeException() {
            val exception = BusinessException("오류")

            assertTrue(exception is RuntimeException)
        }
    }
}
