package com.github.s8u.streamarchive.client.soop

import com.fasterxml.jackson.annotation.JsonProperty

data class SoopStationDto(
    @JsonProperty("station_no")
    val stationNo: Long,
    @JsonProperty("user_id")
    val userId: String,
    @JsonProperty("user_nick")
    val userNick: String,
    @JsonProperty("station_name")
    val stationName: String?,
    @JsonProperty("station_title")
    val stationTitle: String?,
    @JsonProperty("grade")
    val grade: Int?,
    @JsonProperty("broad_start")
    val broadStart: String?,
    @JsonProperty("total_broad_time")
    val totalBroadTime: Long?
)