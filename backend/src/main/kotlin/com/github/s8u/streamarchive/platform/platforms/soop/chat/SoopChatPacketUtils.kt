package com.github.s8u.streamarchive.platform.platforms.soop.chat

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

/**
 * SOOP 채팅 패킷 유틸
 */
object SoopChatPacketUtils {

    private val charset: Charset = StandardCharsets.UTF_8

    /**
     * SOOP 채팅 패킷 바이트 배열을 생성한다.
     */
    fun makePacket(
        commandType: SoopChatCommandType,
        body: List<Any?>
    ): ByteArray {
        val bodyBytes = body
            .joinToString("") { item ->
                when (item) {
                    null -> ""
                    is ByteArray -> String(item, charset)
                    else -> item.toString()
                }
            }
            .toByteArray(charset)

        val header = buildString {
            append(ESC)
            append(TAB)
            append(commandType.value.toString().padStart(4, '0'))
            append(bodyBytes.size.toString().padStart(6, '0'))
            append("00")
        }.toByteArray(charset)

        return header + bodyBytes
    }

    /**
     * SOOP 채팅 패킷 바이트 배열을 파싱한다.
     *
     * 헤더를 읽을 수 없으면 null을 반환한다.
     */
    fun parsePacket(bytes: ByteArray): SoopChatPacketDto? {
        if (bytes.size < HEADER_LENGTH) return null

        val serviceCode = bytes
            .copyOfRange(2, 6)
            .toString(charset)
            .toIntOrNull() ?: return null
        val resultCode = bytes
            .copyOfRange(12, 14)
            .toString(charset)
            .toIntOrNull() ?: return null
        val body = bytes.copyOfRange(HEADER_LENGTH, bytes.size)

        return SoopChatPacketDto(
            serviceCode = serviceCode,
            resultCode = resultCode,
            packet = parseBody(body)
        )
    }

    private fun parseBody(body: ByteArray): List<String> {
        val fields = mutableListOf<String>()
        var start = 1

        for (index in 1 until body.size) {
            if (body[index] != FORM_FEED_CODE) continue

            fields.add(body.copyOfRange(start, index).toString(charset))
            start = index + 1
        }

        if (start < body.size) {
            fields.add(body.copyOfRange(start, body.size).toString(charset))
        }

        return fields
    }

    const val FORM_FEED: String = "\u000C"

    private const val HEADER_LENGTH = 14
    private const val ESC = "\u001B"
    private const val TAB = "\u0009"
    private const val FORM_FEED_CODE = 12.toByte()

}
