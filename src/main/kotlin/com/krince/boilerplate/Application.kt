package com.krince.boilerplate

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
class BoilerplateApplication

fun main(args: Array<String>) {
    runApplication<BoilerplateApplication>(*args)
}
