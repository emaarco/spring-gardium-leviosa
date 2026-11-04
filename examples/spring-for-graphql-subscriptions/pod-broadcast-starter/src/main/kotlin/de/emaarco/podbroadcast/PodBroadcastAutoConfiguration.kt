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

@AutoConfiguration(after = [KafkaAutoConfiguration::class, JacksonAutoConfiguration::class])
@EnableConfigurationProperties(PodBroadcastProperties::class)
class PodBroadcastAutoConfiguration {

    @Bean
    fun podBroadcastTopic(properties: PodBroadcastProperties) =
        TopicBuilder.name(properties.topic).partitions(3).build()

    @Bean
    fun podBroadcaster(
        kafkaTemplate: KafkaTemplate<String, String>,
        jsonMapper: JsonMapper,
        properties: PodBroadcastProperties,
    ) = PodBroadcaster(kafkaTemplate, jsonMapper, properties.topic)

    @Bean
    fun podBroadcastListener(
        broadcaster: PodBroadcaster,
        properties: PodBroadcastProperties,
        @Value("\${spring.application.name}") applicationName: String,
    ) = PodBroadcastListener(broadcaster, properties.topic, "$applicationName-${properties.podName}")

    @Bean
    fun podBroadcastHealthIndicator(registry: KafkaListenerEndpointRegistry) = PodBroadcastHealthIndicator(registry)
}
