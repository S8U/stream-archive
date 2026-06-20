package com.github.s8u.streamarchive.global.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

/**
 * JPA Auditing 설정
 *
 * 엔티티의 @CreatedDate·@LastModifiedDate가 동작하게 한다.
 */
@Configuration
@EnableJpaAuditing
class JpaConfig
