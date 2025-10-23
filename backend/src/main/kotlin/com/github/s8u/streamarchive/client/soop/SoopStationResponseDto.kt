package com.github.s8u.streamarchive.client.soop

import com.fasterxml.jackson.annotation.JsonProperty

data class SoopStationResponseDto(
    @JsonProperty("profile_image")
    val profileImage: String?,
    @JsonProperty("station")
    val station: SoopStationDto?,
    @JsonProperty("broad")
    val broad: SoopBroadDto?
)
