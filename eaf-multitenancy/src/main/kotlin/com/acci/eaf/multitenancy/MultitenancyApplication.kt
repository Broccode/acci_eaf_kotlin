package com.acci.eaf.multitenancy

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MultitenancyApplication

fun main(args: Array<String>) {
    runApplication<MultitenancyApplication>(*args)
}
