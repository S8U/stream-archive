package com.github.s8u.streamarchive.user.usecase

import com.github.s8u.streamarchive.auth.security.service.CurrentUserService
import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.user.service.UserSettingLoadService
import com.github.s8u.streamarchive.user.usecase.dto.result.UserSettingGetResult
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 내 설정 조회
 *
 * 저장하지 않은 설정과 권한이 없는 관리자 전용 설정은 기본값으로 보여준다.
 */
@Service
class UserSettingGetUseCase(
    private val currentUserService: CurrentUserService,
    private val userSettingLoadService: UserSettingLoadService
) {

    @Transactional(readOnly = true)
    fun get(): UserSettingGetResult {
        if (!currentUserService.isAuthenticated())
            throw BusinessException("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED)

        return UserSettingGetResult.from(userSettingLoadService.load())
    }

}
