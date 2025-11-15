package com.github.s8u.streamarchive

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
class StreamArchiveApplication

fun main(args: Array<String>) {
    runApplication<StreamArchiveApplication>(*args)
}
