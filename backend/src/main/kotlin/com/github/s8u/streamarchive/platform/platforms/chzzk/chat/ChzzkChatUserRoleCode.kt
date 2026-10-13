package com.github.s8u.streamarchive.platform.platforms.chzzk.chat

import com.github.s8u.streamarchive.platform.enums.PlatformChatUserRole

/**
 * 치지직 채팅 작성자 역할 코드
 *
 * 채팅 프로필의 `userRoleCode` 값과 그에 대응하는 공통 역할을 담는다.
 * 목록에 없는 값은 일반 시청자로 본다.
 */
enum class ChzzkChatUserRoleCode(
    val value: String,
    val userRole: PlatformChatUserRole?
) {
    COMMON_USER("common_user", null),                                                           // 일반 시청자
    STREAMER("streamer", PlatformChatUserRole.STREAMER),                                        // 스트리머
    STREAMING_CHAT_MANAGER("streaming_chat_manager", PlatformChatUserRole.MANAGER),             // 채팅 운영자
    STREAMING_CHANNEL_MANAGER("streaming_channel_manager", PlatformChatUserRole.MANAGER),       // 채널 관리자
    STREAMING_SETTLEMENT_MANAGER("streaming_settlement_manager", PlatformChatUserRole.MANAGER), // 정산 관리자 (실측 미확인)
    MANAGER("manager", PlatformChatUserRole.MANAGER);                                           // 매니저 (실측 미확인)

    companion object {
        fun fromValue(value: String): ChzzkChatUserRoleCode? {
            return entries.find { it.value == value }
        }
    }

}
