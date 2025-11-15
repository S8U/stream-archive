package com.github.s8u.streamarchive.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Configuration
class ExecutorConfig {

    @Bean
    fun recordingExecutorService(): ExecutorService {
        return Executors.newCachedThreadPool()
    }
}
