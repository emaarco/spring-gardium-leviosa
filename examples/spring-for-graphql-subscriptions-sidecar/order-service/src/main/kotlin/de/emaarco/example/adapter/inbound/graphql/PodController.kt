package de.emaarco.example.adapter.inbound.graphql

import org.springframework.beans.factory.annotation.Value
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class PodController(
    @Value("\${pod.name}") private val podName: String,
) {
    @QueryMapping
    fun pod() = podName
}
