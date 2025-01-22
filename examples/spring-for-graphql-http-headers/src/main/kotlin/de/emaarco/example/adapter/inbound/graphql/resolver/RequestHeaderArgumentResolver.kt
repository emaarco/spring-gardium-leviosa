package de.emaarco.example.adapter.inbound.graphql.resolver

import graphql.schema.DataFetchingEnvironment
import org.springframework.core.MethodParameter
import org.springframework.graphql.data.method.HandlerMethodArgumentResolver
import org.springframework.web.bind.annotation.RequestHeader

/**
 * Resolves method arguments annotated with `@RequestHeader`
 * by looking up the value in the GraphQL context.
 */
class RequestHeaderArgumentResolver : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter) =
        parameter.hasParameterAnnotation(RequestHeader::class.java)

    override fun resolveArgument(
        parameter: MethodParameter,
        environment: DataFetchingEnvironment,
    ): Any? {
        val requiredHeader = getNameOfRequiredHeader(parameter)
        return environment.graphQlContext.get(requiredHeader)
    }

    private fun getNameOfRequiredHeader(parameter: MethodParameter): String {
        val headerAnnotation = parameter.getParameterAnnotation(RequestHeader::class.java)
        return headerAnnotation?.name ?: throw IllegalArgumentException(
            "No header name specified for @RequestHeader annotation",
        )
    }
}
