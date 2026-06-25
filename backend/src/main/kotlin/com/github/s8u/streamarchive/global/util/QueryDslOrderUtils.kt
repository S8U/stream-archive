package com.github.s8u.streamarchive.global.util

import com.querydsl.core.types.OrderSpecifier
import org.springframework.data.domain.Pageable

/**
 * QueryDSL 정렬 변환 유틸
 *
 * Pageable 정렬 중 허용된 필드만 OrderSpecifier로 바꾸고, 없으면 기본 정렬을 사용한다.
 */
object QueryDslOrderUtils {

    fun getOrderSpecifiers(
        pageable: Pageable,
        orderBuilders: Map<String, (Boolean) -> OrderSpecifier<*>>,
        defaultOrders: List<OrderSpecifier<*>>,
        tieBreaker: OrderSpecifier<*>? = null
    ): Array<OrderSpecifier<*>> {
        if (pageable.sort.isUnsorted) {
            return defaultOrders.toTypedArray()
        }

        val orders = pageable.sort
            .mapNotNull { order -> orderBuilders[order.property]?.invoke(order.isAscending) }

        if (orders.isEmpty()) {
            return defaultOrders.toTypedArray()
        }

        // 중복값 컬럼 정렬 시 offset/limit 페이징의 행 중복·누락을 막기 위함
        return (orders + listOfNotNull(tieBreaker)).toTypedArray()
    }

}
