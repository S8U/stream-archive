package com.github.s8u.streamarchive.exception

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(ex: BusinessException): ResponseEntity<ErrorResponse> {
        logger.warn("Business exception: {}", ex.message)
        return ResponseEntity
            .status(ex.status)
            .body(ErrorResponse(ex.message ?: "Business error", ex.status.value()))
    }

    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): ResponseEntity<ErrorResponse> {
        logger.error("Unexpected exception", ex)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR.value()))
    }

    data class ErrorResponse(
        val message: String,
        val status: Int
    )
}
