package com.github.s8u.streamarchive.global.service

import org.slf4j.LoggerFactory
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.stereotype.Component
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

/**
 * 짧은 쓰기 트랜잭션 실행기
 *
 * 느린 I/O와 트랜잭션을 분리해야 하는 흐름에서, 진입점 UseCase가 트랜잭션 경계를 직접 소유하도록 돕는다.
 * 동시 갱신으로 낙관적 락 충돌이 나면 최신 상태로 다시 실행한다.
 */
@Component
class TransactionRunner(
    transactionManager: PlatformTransactionManager
) {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val transactionTemplate = TransactionTemplate(transactionManager)

    companion object {
        private const val MAX_ATTEMPTS = 3
    }

    /**
     * 트랜잭션 안에서 실행하고 결과를 반환한다.
     */
    fun <T> run(action: () -> T): T {
        return transactionTemplate.execute { action() } as T
    }

    /**
     * 트랜잭션 안에서 실행하고 결과를 반환한다.
     *
     * 낙관적 락 충돌 시 [MAX_ATTEMPTS]회까지 다시 실행한다.
     */
    fun <T> runWithRetry(action: () -> T): T {
        var lastException: OptimisticLockingFailureException? = null

        repeat(MAX_ATTEMPTS) { attempt ->
            try {
                return transactionTemplate.execute { action() } as T
            } catch (e: OptimisticLockingFailureException) {
                lastException = e
                logger.debug("TransactionRunner: Optimistic lock conflict, retrying: attempt={}", attempt + 1)
            }
        }

        throw lastException!!
    }

}
