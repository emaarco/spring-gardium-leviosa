package de.emaarco.example.adapter.`in`.graphql

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.core.io.ClassPathResource
import org.springframework.graphql.ExecutionGraphQlService
import org.springframework.graphql.support.DocumentSource
import org.springframework.graphql.support.ResourceDocumentSource
import org.springframework.graphql.test.tester.ExecutionGraphQlServiceTester
import org.springframework.graphql.test.tester.GraphQlTester
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder

@Configuration
class GraphQlTestConfiguration {

    @Bean
    @Primary
    fun customGraphQlTester(
        executionGraphQlTester: ExecutionGraphQlService,
    ): GraphQlTester {
        val customDocumentSource = documentSource()
        return ExecutionGraphQlServiceTester.builder(executionGraphQlTester)
            .documentSource(customDocumentSource)
            .encoder(Jackson2JsonEncoder())
            .decoder(Jackson2JsonDecoder())
            .build()
    }

    private fun documentSource(): DocumentSource {
        val resources = listOf(ClassPathResource("graphql-test/"), ClassPathResource("graphql-fragments/"))
        return ResourceDocumentSource(resources, ResourceDocumentSource.FILE_EXTENSIONS)
    }
}