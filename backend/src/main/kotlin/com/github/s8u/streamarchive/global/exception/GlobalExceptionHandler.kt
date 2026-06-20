package com.github.s8u.streamarchive.global.exception

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.async.AsyncRequestNotUsableException

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(ex: BusinessException): ResponseEntity<ErrorResponse> {
        logger.warn("GlobalExceptionHandler: Business exception: {}", ex.message)
        return ResponseEntity
            .status(ex.status)
            .body(ErrorResponse(ex.message ?: "Business error", ex.status.value()))
    }

    @ExceptionHandler(AsyncRequestNotUsableException::class)
    fun handleClientDisconnect(ex: AsyncRequestNotUsableException) {
        // 클라이언트가 응답을 받기 전에 연결을 끊은 경우다 (HLS 재생 중단 등)
        // 응답을 쓸 곳이 없으므로 본문 없이 조용히 넘긴다
        logger.debug("GlobalExceptionHandler: Client disconnected: {}", ex.message)
    }

    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): ResponseEntity<ErrorResponse> {
        logger.error("GlobalExceptionHandler: Unexpected exception", ex)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR.value()))
    }

    data class ErrorResponse(
        val message: String,
        val status: Int
    )
}
