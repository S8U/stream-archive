package com.github.s8u.streamarchive.client.soop

import com.fasterxml.jackson.annotation.JsonProperty

data class SoopLiveDetailDto(
    @JsonProperty("RESULT")
    val result: Int,
    @JsonProperty("BNO")
    val bno: String?,
    @JsonProperty("BJID")
    val bjid: String?,
    @JsonProperty("BJNICK")
    val bjnick: String?,
    @JsonProperty("TITLE")
    val title: String?,
    @JsonProperty("CATE")
    val cate: String?,
    @JsonProperty("BTIME")
    val btime: Int?,
    @JsonProperty("RESOLUTION")
    val resolution: String?,
    @JsonProperty("BPS")
    val bps: String?
)
