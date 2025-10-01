package com.github.s8u.streamarchive.runner

import com.github.s8u.streamarchive.service.GlobalSettingService
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class GlobalSettingInitRunner(
    private val globalSettingService: GlobalSettingService
) : ApplicationRunner {
    private val logger = LoggerFactory.getLogger(GlobalSettingInitRunner::class.java)

    override fun run(args: ApplicationArguments) {
        logger.info("Initializing global settings")

        globalSettingService.initializeDefaultSettings()

        logger.info("Global settings initialization completed")
    }
}