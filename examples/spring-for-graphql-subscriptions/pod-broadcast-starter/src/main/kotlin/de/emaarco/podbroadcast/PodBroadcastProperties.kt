package de.emaarco.podbroadcast

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Configuration under `pod-broadcast`: [podName] must be unique per pod (e.g. the Kubernetes pod name),
 * [topic] is the Kafka topic that carries the events of all pods.
 */
@ConfigurationProperties("pod-broadcast")
data class PodBroadcastProperties(
    val podName: String,
    val topic: String = "pod-broadcast",
)
