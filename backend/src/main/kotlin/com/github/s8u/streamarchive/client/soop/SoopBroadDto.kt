package com.github.s8u.streamarchive.client.soop

import com.fasterxml.jackson.annotation.JsonProperty

data class SoopBroadDto(
    @JsonProperty("user_id")
    val userId: String,
    @JsonProperty("broad_no")
    val broadNo: Long,
    @JsonProperty("broad_cate_no")
    val broadCateNo: Long,
    @JsonProperty("broad_title")
    val broadTitle: String,
    @JsonProperty("current_sum_viewer")
    val currentSumViewer: Int,
    @JsonProperty("broad_grade")
    val broadGrade: Int,
    @JsonProperty("subscription_only")
    val subscriptionOnly: Int,
    @JsonProperty("is_password")
    val isPassword: Boolean
)
