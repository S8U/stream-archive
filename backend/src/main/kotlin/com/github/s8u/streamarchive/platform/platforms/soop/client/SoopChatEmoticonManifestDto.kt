package com.github.s8u.streamarchive.platform.platforms.soop.client

import com.fasterxml.jackson.annotation.JsonProperty

data class SoopChatEmoticonManifestDto(
    @JsonProperty("default")
    val defaultEmoticons: List<SoopChatEmoticonDto> = emptyList(),

    @JsonProperty("subscribe")
    val subscribeEmoticons: List<SoopChatEmoticonDto> = emptyList()
)
