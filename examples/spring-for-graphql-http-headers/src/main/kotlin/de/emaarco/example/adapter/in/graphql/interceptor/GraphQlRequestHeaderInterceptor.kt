package de.emaarco.example.adapter.`in`.graphql.interceptor

import mu.KLogger
import mu.KotlinLogging
import org.springframework.graphql.server.WebGraphQlInterceptor
import org.springframework.graphql.server.WebGraphQlInterceptor.Chain
import org.springframework.graphql.server.WebGraphQlRequest
import org.springframework.graphql.server.WebGraphQlResponse
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 * Interceptor that extracts headers from the request and adds them to the GraphQL context.
 * This allows you to access them in your GraphQL resolvers via '@ContextValue'.
 */
@Component
class GraphQlRequestHeaderInterceptor(
    private val log: KLogger = KotlinLogging.logger {}
) : WebGraphQlInterceptor {

    override fun intercept(request: WebGraphQlRequest, chain: Chain): Mono<WebGraphQlResponse> {
        val headers = getHeadersFromRequest(request)
        log.trace { "Found ${headers.size} headers that will be added to the GQL-context: $headers" }
        addHeadersToGraphQLContext(request, headers)
        return chain.next(request)
    }

    private fun getHeadersFromRequest(request: WebGraphQlRequest): Map<String, Any> {
        return request.headers.mapValues { it.value.first() }
    }

    private fun addHeadersToGraphQLContext(
        request: WebGraphQlRequest, customHeaders: Map<String, Any>
    ) = request.configureExecutionInput { _, builder ->
        builder.graphQLContext(customHeaders).build()
    }

}
