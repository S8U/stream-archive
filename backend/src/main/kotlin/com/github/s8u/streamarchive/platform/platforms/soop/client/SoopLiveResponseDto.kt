package com.github.s8u.streamarchive.platform.platforms.soop.client

import com.fasterxml.jackson.annotation.JsonProperty

data class SoopLiveResponseDto(
    @JsonProperty("CHANNEL")
    val channel: SoopLiveDetailDto?
)
