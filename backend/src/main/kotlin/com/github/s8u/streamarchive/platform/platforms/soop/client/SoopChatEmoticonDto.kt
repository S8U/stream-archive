package com.github.s8u.streamarchive.platform.platforms.soop.client

data class SoopChatEmoticonDto(
    val keyword: String,
    val fileName: String?,
    val staticFileName: String? = null,
    val isDeprecated: Boolean = false,
    val version: Int? = null
)
