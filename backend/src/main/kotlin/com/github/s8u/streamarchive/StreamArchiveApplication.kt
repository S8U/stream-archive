package com.github.s8u.streamarchive

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class StreamArchiveApplication

fun main(args: Array<String>) {
    runApplication<StreamArchiveApplication>(*args)
}
