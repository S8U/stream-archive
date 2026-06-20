package com.github.s8u.streamarchive.recording.manager

import com.github.s8u.streamarchive.platform.chat.dto.PlatformChatMessageDto
import org.springframework.stereotype.Component
import java.util.Collections

/**
 * 녹화 중 수집한 채팅 메시지 버퍼
 *
 * WebSocket으로 들어온 채팅을 메모리에 모았다가, 스케줄러가 주기적으로 비워 배치 저장한다.
 * 적재 진입점(WebSocket)과 flush 진입점(스케줄러)이 공유하는 상태 컴포넌트다.
 */
@Component
class RecordingChatBufferManager {

    private val chatBuffer: MutableList<PlatformChatMessageDto> = Collections.synchronizedList(mutableListOf())

    /**
     * 채팅 메시지 버퍼에 추가
     */
    fun add(chatMessageDto: PlatformChatMessageDto) {
        chatBuffer.add(chatMessageDto)
    }

    /**
     * 버퍼에 쌓인 메시지를 모두 꺼내고 비운다.
     */
    fun drain(): List<PlatformChatMessageDto> {
        synchronized(chatBuffer) {
            if (chatBuffer.isEmpty()) return emptyList()

            val items = chatBuffer.toList()
            chatBuffer.clear()
            return items
        }
    }

}
