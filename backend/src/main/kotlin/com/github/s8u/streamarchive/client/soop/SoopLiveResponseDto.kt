package com.github.s8u.streamarchive.client.soop

import com.fasterxml.jackson.annotation.JsonProperty

data class SoopLiveResponseDto(
    @JsonProperty("CHANNEL")
    val channel: SoopLiveDetailDto?
)
