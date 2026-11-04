package de.emaarco.sidecar.adapter.inbound.kafka

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder

@Configuration
class RelayTopicConfiguration {

    @Bean
    fun relayTopic(
        @Value("\${relay.topic}") topic: String,
    ) = TopicBuilder.name(topic).partitions(3).build()
}
