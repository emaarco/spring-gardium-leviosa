package de.emaarco.podbroadcast

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("pod-broadcast")
data class PodBroadcastProperties(
    val podName: String,
    val topic: String = "pod-broadcast",
)
