package com.github.s8u.streamarchive.user.service

import com.github.s8u.streamarchive.auth.security.service.CurrentUserService
import com.github.s8u.streamarchive.user.enums.UserSettingKey
import com.github.s8u.streamarchive.user.repository.UserSettingRepository
import com.github.s8u.streamarchive.user.service.dto.UserSettingLoadResult
import org.springframework.stereotype.Service

/**
 * 사용자 설정 불러오기 서비스
 *
 * 다른 도메인(동영상 목록 등)이 현재 사용자의 설정에 따라 다르게 동작할 때 쓴다.
 */
@Service
class UserSettingLoadService(
    private val userSettingRepository: UserSettingRepository,
    private val currentUserService: CurrentUserService
) {

    /**
     * 현재 로그인 사용자에게 적용할 설정 값을 불러온다.
     *
     * 비로그인이면 모든 설정에 기본값을 쓴다.
     */
    fun load(): UserSettingLoadResult {
        val userId = currentUserService.getCurrentUserId()
        val isAdmin = currentUserService.isAdmin()

        val savedValues = userId
            ?.let { userSettingRepository.findAllByUserId(it) }
            .orEmpty()
            .associate { it.settingKey to it.settingValue }

        val values = UserSettingKey.entries.associateWith { key ->
            key.resolveValue(savedValues[key], isAdmin)
        }

        return UserSettingLoadResult.from(values)
    }

}
