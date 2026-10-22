package com.github.s8u.streamarchive.video.enums

enum class VideoContentPrivacy {
    PUBLIC,
    UNLISTED,
    PRIVATE;

    companion object {
        /**
         * 공개 목록(홈·검색·채널 동영상 목록)에 보일 공개 범위를 반환한다.
         *
         * 일부공개는 [isShowUnlisted]일 때만 넣는다.
         * 비공개는 넣지 않는다.
         */
        fun getListedPrivacies(isShowUnlisted: Boolean): List<VideoContentPrivacy> {
            return if (isShowUnlisted) listOf(PUBLIC, UNLISTED) else listOf(PUBLIC)
        }
    }

}
