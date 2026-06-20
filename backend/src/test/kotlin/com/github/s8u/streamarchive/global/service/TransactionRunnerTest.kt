package com.github.s8u.streamarchive.global.service

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionStatus
import kotlin.test.assertEquals

class TransactionRunnerTest {

    // 트랜잭션은 통과시키고, 실제 재시도 루프 동작만 검증한다
    private val transactionManager = mockk<PlatformTransactionManager>(relaxed = true)
    private val transactionRunner = TransactionRunner(transactionManager)

    init {
        every { transactionManager.getTransaction(any()) } returns mockk<TransactionStatus>(relaxed = true)
    }

    @Nested
    inner class Run {

        @Test
        fun `액션을 실행하고 결과를 반환한다`() {
            val result = transactionRunner.run { "결과" }

            assertEquals("결과", result)
        }

        @Test
        fun `액션이 던진 예외를 그대로 전파한다`() {
            assertThrows<IllegalStateException> {
                transactionRunner.run { throw IllegalStateException() }
            }
        }
    }

    @Nested
    inner class RunWithRetry {

        @Test
        fun `한 번에 성공하면 한 번만 실행한다`() {
            var attempts = 0

            val result = transactionRunner.runWithRetry {
                attempts++
                "결과"
            }

            assertEquals(1, attempts)
            assertEquals("결과", result)
        }

        @Test
        fun `낙관적 락 충돌이 나면 다시 시도해 성공한다`() {
            var attempts = 0

            val result = transactionRunner.runWithRetry {
                attempts++
                if (attempts < 3) {
                    throw OptimisticLockingFailureException("충돌")
                }
                "결과"
            }

            assertEquals(3, attempts)
            assertEquals("결과", result)
        }

        @Test
        fun `최대 횟수까지 충돌하면 마지막 예외를 던진다`() {
            var attempts = 0

            assertThrows<OptimisticLockingFailureException> {
                transactionRunner.runWithRetry {
                    attempts++
                    throw OptimisticLockingFailureException("충돌")
                }
            }

            assertEquals(3, attempts)
        }

        @Test
        fun `낙관적 락 외의 예외는 재시도하지 않고 바로 전파한다`() {
            var attempts = 0

            assertThrows<IllegalStateException> {
                transactionRunner.runWithRetry {
                    attempts++
                    throw IllegalStateException()
                }
            }

            assertEquals(1, attempts)
        }
    }

}
