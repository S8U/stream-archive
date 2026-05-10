package com.github.s8u.streamarchive.dto

import com.github.s8u.streamarchive.entity.VideoMetadataViewerHistory

data class ViewerHistoryResponse(
    val viewerCount: Int,
    val offsetMillis: Long
) {
    companion object {
        fun from(viewerHistory: VideoMetadataViewerHistory): ViewerHistoryResponse {
            return ViewerHistoryResponse(
                viewerCount = viewerHistory.viewerCount,
                offsetMillis = viewerHistory.offsetMillis
            )
        }
    }
}
