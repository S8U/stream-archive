package com.github.s8u.streamarchive.user.usecase

import com.github.s8u.streamarchive.auth.security.service.CurrentUserService
import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.global.util.RequestUtils
import com.github.s8u.streamarchive.user.entity.UserSetting
import com.github.s8u.streamarchive.user.enums.UserSettingKey
import com.github.s8u.streamarchive.user.repository.UserSettingRepository
import com.github.s8u.streamarchive.user.service.UserSettingLoadService
import com.github.s8u.streamarchive.user.usecase.dto.command.UserSettingUpdateCommand
import com.github.s8u.streamarchive.user.usecase.dto.result.UserSettingUpdateResult
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 내 설정 수정
 *
 * 요청에 담긴 설정만 바꾼다.
 * 저장한 적 없는 설정은 새로 만들고, 있으면 값을 바꾼다.
 */
@Service
class UserSettingUpdateUseCase(
    private val userSettingRepository: UserSettingRepository,
    private val currentUserService: CurrentUserService,
    private val userSettingLoadService: UserSettingLoadService
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun update(command: UserSettingUpdateCommand): UserSettingUpdateResult {
        val userId = currentUserService.getCurrentUserId()
            ?: throw BusinessException("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED)
        val isAdmin = currentUserService.isAdmin()
        val changedValues = command.toChangedValues()

        // 검증
        if (changedValues.keys.any { !it.isAvailableTo(isAdmin) })
            throw BusinessException("관리자만 바꿀 수 있는 설정입니다.", HttpStatus.FORBIDDEN)

        // 없으면 생성, 있으면 수정
        val clientIp = RequestUtils.getClientIp()
        val savedSettings = userSettingRepository.findAllByUserIdAndSettingKeyIn(userId, UserSettingKey.entries)
            .associateBy { it.settingKey }
        changedValues.forEach { (key, value) ->
            val setting = savedSettings[key]
            if (setting == null) {
                val newSetting = UserSetting(
                    userId = userId,
                    settingKey = key,
                    settingValue = value
                )
                newSetting.recordCreator(userId, clientIp)
                userSettingRepository.save(newSetting)
            } else {
                setting.changeValue(value)
                setting.recordUpdater(userId, clientIp)
            }
        }

        logger.info("UserSettingUpdateUseCase: User settings updated: userId={} keys={}", userId, changedValues.keys)

        return UserSettingUpdateResult.from(userSettingLoadService.load())
    }

}
