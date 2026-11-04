package de.emaarco.sidecar

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class EventRelaySidecarApplication

fun main(args: Array<String>) {
    runApplication<EventRelaySidecarApplication>(*args)
}
