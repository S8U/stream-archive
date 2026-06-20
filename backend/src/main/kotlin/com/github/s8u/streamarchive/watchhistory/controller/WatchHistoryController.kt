package com.github.s8u.streamarchive.watchhistory.controller

import com.github.s8u.streamarchive.watchhistory.controller.dto.response.WatchHistorySearchResponse
import com.github.s8u.streamarchive.watchhistory.usecase.WatchHistoryClearUseCase
import com.github.s8u.streamarchive.watchhistory.usecase.WatchHistoryDeleteUseCase
import com.github.s8u.streamarchive.watchhistory.usecase.WatchHistorySearchUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@Tag(name = "WatchHistory", description = "시청 기록")
@RestController
@RequestMapping("/watch-histories")
class WatchHistoryController(
    private val watchHistorySearchUseCase: WatchHistorySearchUseCase,
    private val watchHistoryDeleteUseCase: WatchHistoryDeleteUseCase,
    private val watchHistoryClearUseCase: WatchHistoryClearUseCase
) {

    @Operation(summary = "시청 기록 목록 조회")
    @GetMapping
    fun getWatchHistories(pageable: Pageable): Page<WatchHistorySearchResponse> {
        return watchHistorySearchUseCase.search(pageable)
            .map { WatchHistorySearchResponse.from(it) }
    }

    @Operation(summary = "시청 기록 개별 삭제")
    @DeleteMapping("/{videoUuid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteWatchHistory(@PathVariable videoUuid: String) {
        watchHistoryDeleteUseCase.delete(videoUuid)
    }

    @Operation(summary = "시청 기록 전체 삭제")
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteAllWatchHistories() {
        watchHistoryClearUseCase.clear()
    }

}
