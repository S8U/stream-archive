package com.github.s8u.streamarchive.config

import com.github.s8u.streamarchive.config.properties.JwtProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(JwtProperties::class)
class JwtConfig
