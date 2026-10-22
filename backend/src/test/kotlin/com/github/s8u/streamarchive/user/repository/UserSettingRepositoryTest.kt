package com.github.s8u.streamarchive.user.repository

import com.github.s8u.streamarchive.user.entity.UserSetting
import com.github.s8u.streamarchive.user.enums.UserSettingKey
import jakarta.persistence.EntityManager
import java.time.LocalDateTime
import kotlin.test.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserSettingRepositoryTest @Autowired constructor(
    private val userSettingRepository: UserSettingRepository,
    private val entityManager: EntityManager
) {

    @Nested
    inner class FindAllByUserIdAndSettingKeyIn {

        @Test
        fun `없앤 키의 행이 남아 있어도 지금 있는 키만 조회한다`() {
            userSettingRepository.save(setting(userId = USER_ID, settingKey = UserSettingKey.SHOW_UNLISTED_VIDEOS))
            userSettingRepository.save(
                setting(userId = OTHER_USER_ID, settingKey = UserSettingKey.SHOW_UNLISTED_CHANNELS)
            )
            insertRemovedKeySetting(userId = USER_ID, settingKey = "content.show_all_channels")
            entityManager.flush()
            entityManager.clear()

            val settings = userSettingRepository.findAllByUserIdAndSettingKeyIn(USER_ID, UserSettingKey.entries)

            assertEquals(listOf(UserSettingKey.SHOW_UNLISTED_VIDEOS), settings.map { it.settingKey })
        }
    }

    private fun setting(
        userId: Long,
        settingKey: UserSettingKey
    ): UserSetting {
        return UserSetting(
            userId = userId,
            settingKey = settingKey,
            settingValue = "true"
        )
    }

    // 없앤 키는 엔티티로 저장할 수 없어 SQL로 넣는다
    private fun insertRemovedKeySetting(
        userId: Long,
        settingKey: String
    ) {
        val now = LocalDateTime.now()
        entityManager.createNativeQuery(
            """
            INSERT INTO user_settings (user_id, setting_key, setting_value, created_at, updated_at)
            VALUES (:userId, :settingKey, 'true', :now, :now)
            """
        )
            .setParameter("userId", userId)
            .setParameter("settingKey", settingKey)
            .setParameter("now", now)
            .executeUpdate()
    }

    companion object {
        private const val USER_ID = 1L
        private const val OTHER_USER_ID = 2L
    }

}
