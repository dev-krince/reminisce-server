package com.krince.reminisce

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
class ReminisceApplication

fun main(args: Array<String>) {
    runApplication<ReminisceApplication>(*args)
}
