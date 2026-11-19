package com.github.s8u.streamarchive.platform.platforms.chzzk.properties

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * 치지직 설정값
 *
 * 네이버 로그인 쿠키를 넣으면 연령 제한 방송처럼 로그인해야 볼 수 있는 방송의 영상, 썸네일, 채팅도 받는다.
 * 비워 두면 로그인 없이 받아서 그런 방송은 녹화에 실패한다.
 */
@ConfigurationProperties(prefix = "platform.chzzk")
data class ChzzkProperties(
    val nidAut: String,
    val nidSes: String
) {

    /**
     * 네이버 로그인 쿠키가 둘 다 있는지 확인합니다.
     */
    fun hasLoginCookie(): Boolean {
        return nidAut.isNotBlank() && nidSes.isNotBlank()
    }

}
