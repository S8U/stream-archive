package com.github.s8u.streamarchive.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "url")
data class UrlProperties(
    var apiBase: String = "http://localhost:8080",
    var frontendBase: String = "http://localhost:3000"
)