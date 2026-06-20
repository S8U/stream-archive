package com.github.s8u.streamarchive.global.config

import com.github.s8u.streamarchive.auth.jwt.properties.JwtProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(JwtProperties::class)
class JwtConfig
