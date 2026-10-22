package com.github.s8u.streamarchive.channel.enums

enum class ChannelContentPrivacy {
    PUBLIC,
    UNLISTED,
    PRIVATE;

    companion object {
        /**
         * 공개 목록(사이드바, 홈·검색 동영상)에 보일 채널 공개 범위를 반환한다.
         *
         * 일부공개는 [isShowUnlisted]일 때만 넣는다.
         * 비공개는 넣지 않는다.
         */
        fun getListedPrivacies(isShowUnlisted: Boolean): List<ChannelContentPrivacy> {
            return if (isShowUnlisted) listOf(PUBLIC, UNLISTED) else listOf(PUBLIC)
        }
    }

}
