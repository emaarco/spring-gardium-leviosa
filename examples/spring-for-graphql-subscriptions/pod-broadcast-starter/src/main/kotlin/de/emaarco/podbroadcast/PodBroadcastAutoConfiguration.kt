package de.emaarco.podbroadcast

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration
import org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.kafka.config.KafkaListenerEndpointRegistry
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.KafkaTemplate
import tools.jackson.databind.json.JsonMapper

/**
 * Registers everything a service needs to broadcast events to all of its pods:
 * the topic, the [PodBroadcastPublisher], the [PodBroadcastReceiver], the [PodBroadcastKafkaListener]
 * that connects both via Kafka, and the [PodBroadcastHealthIndicator] for readiness.
 */
@AutoConfiguration(after = [KafkaAutoConfiguration::class, JacksonAutoConfiguration::class])
@EnableConfigurationProperties(PodBroadcastProperties::class)
class PodBroadcastAutoConfiguration {

    @Bean
    fun podBroadcastTopic(properties: PodBroadcastProperties) =
        TopicBuilder.name(properties.topic).partitions(3).build()

    @Bean
    fun podBroadcastPublisher(
        kafkaTemplate: KafkaTemplate<String, String>,
        jsonMapper: JsonMapper,
        properties: PodBroadcastProperties,
    ) = PodBroadcastPublisher(kafkaTemplate, jsonMapper, properties.topic)

    @Bean
    fun podBroadcastReceiver(jsonMapper: JsonMapper) = PodBroadcastReceiver(jsonMapper)

    @Bean
    fun podBroadcastKafkaListener(
        receiver: PodBroadcastReceiver,
        properties: PodBroadcastProperties,
        @Value("\${spring.application.name}") applicationName: String,
    ) = PodBroadcastKafkaListener(receiver, properties.topic, "$applicationName-${properties.podName}")

    @Bean
    fun podBroadcastHealthIndicator(registry: KafkaListenerEndpointRegistry) = PodBroadcastHealthIndicator(registry)
}
