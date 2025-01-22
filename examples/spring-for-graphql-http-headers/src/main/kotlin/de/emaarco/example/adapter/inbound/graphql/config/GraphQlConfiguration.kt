package de.emaarco.example.adapter.inbound.graphql.config

import de.emaarco.example.adapter.inbound.graphql.resolver.RequestHeaderArgumentResolver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.springframework.graphql.data.method.annotation.support.AnnotatedControllerConfigurer

@Configuration
class GraphQlConfiguration {

    @Bean
    @DependsOn("requestHeaderArgumentResolver")
    fun argumentResolverConfiguration(headerResolver: RequestHeaderArgumentResolver) =
        AnnotatedControllerConfigurer().also { configurer ->
            configurer.addCustomArgumentResolver(headerResolver)
        }

    @Bean
    fun requestHeaderArgumentResolver() = RequestHeaderArgumentResolver()
}
