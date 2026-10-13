package com.github.s8u.streamarchive.recording.manager

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.s8u.streamarchive.global.properties.StorageProperties
import com.github.s8u.streamarchive.platform.chat.dto.PlatformChatRawMessageDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.BufferedWriter
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.concurrent.ConcurrentHashMap

/**
 * 녹화 채팅 원본 기록 매니저
 *
 * 녹화별로 플랫폼에서 받은 채팅 원본을 동영상 디렉토리의 JSONL 파일에 이어 쓴다.
 * 한 줄이 원본 메시지 하나다.
 */
@Component
class RecordingChatRawWriteManager(
    private val storageProperties: StorageProperties,
    private val objectMapper: ObjectMapper
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val writers = ConcurrentHashMap<Long, ChatRawFileWriter>()

    /**
     * 녹화의 원본 기록을 시작한다.
     *
     * 이미 기록 중이면 그대로 둔다.
     * 파일이 이미 있으면 이어 쓴다.
     */
    fun open(
        recordId: Long,
        videoId: Long
    ) {
        writers.computeIfAbsent(recordId) {
            ChatRawFileWriter(storageProperties.getVideoChatRawPath(videoId))
        }
    }

    /**
     * 원본 메시지 한 줄을 기록한다.
     *
     * 서버가 비정상 종료돼도 그때까지 받은 원본이 남도록 줄마다 바로 파일에 쓴다.
     * 기록 중이 아닌 녹화의 메시지는 버린다.
     * 기록에 실패하면 그 녹화의 원본 기록만 멈추고 예외를 밖으로 던지지 않는다.
     */
    fun write(
        recordId: Long,
        rawMessageDto: PlatformChatRawMessageDto
    ) {
        val writer = writers[recordId] ?: return

        // 파일 형식이 DTO 변경에 끌려가지 않도록 줄의 필드를 여기서 명시한다
        val line = objectMapper.writeValueAsString(
            linkedMapOf(
                "offsetMillis" to rawMessageDto.offsetMillis,
                "receivedAt" to rawMessageDto.receivedAt.toString(),
                "format" to rawMessageDto.format.name,
                "payload" to rawMessageDto.payload
            )
        )

        try {
            writer.write(line)
        } catch (e: IOException) {
            // 매 메시지마다 실패 로그가 쌓이지 않도록 이 녹화의 기록을 멈춘다
            writers.remove(recordId, writer)
            closeQuietly(recordId, writer)
            logger.error(
                "RecordingChatRawWriteManager: Failed to write chat raw, stopped writing: recordId={}",
                recordId,
                e
            )
        }
    }

    /**
     * 녹화의 원본 기록을 끝내고 파일을 닫는다.
     */
    fun close(recordId: Long) {
        val writer = writers.remove(recordId) ?: return
        closeQuietly(recordId, writer)
    }

    private fun closeQuietly(
        recordId: Long,
        writer: ChatRawFileWriter
    ) {
        try {
            writer.close()
        } catch (e: IOException) {
            logger.error("RecordingChatRawWriteManager: Failed to close chat raw file: recordId={}", recordId, e)
        }
    }

}

// 녹화 하나의 원본 파일 기록기 (빈 파일이 생기지 않도록 첫 기록 때 파일을 연다)
private class ChatRawFileWriter(
    private val path: Path
) {

    private var writer: BufferedWriter? = null

    @Synchronized
    fun write(line: String) {
        val currentWriter = writer ?: open().also { writer = it }
        currentWriter.write(line)
        currentWriter.write("\n")
        currentWriter.flush()
    }

    @Synchronized
    fun close() {
        writer?.close()
        writer = null
    }

    private fun open(): BufferedWriter {
        Files.createDirectories(path.parent)
        return Files.newBufferedWriter(
            path,
            StandardCharsets.UTF_8,
            StandardOpenOption.CREATE,
            StandardOpenOption.APPEND
        )
    }

}
