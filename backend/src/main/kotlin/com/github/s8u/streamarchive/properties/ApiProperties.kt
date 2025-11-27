package com.github.s8u.streamarchive.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "api")
data class ApiProperties(
    var baseUrl: String = "http://localhost:8080"
)