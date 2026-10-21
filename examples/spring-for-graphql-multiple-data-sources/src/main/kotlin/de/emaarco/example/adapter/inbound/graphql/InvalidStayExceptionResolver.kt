package de.emaarco.example.adapter.inbound.graphql

import de.emaarco.example.domain.InvalidStayException
import graphql.GraphQLError
import graphql.GraphqlErrorBuilder
import graphql.schema.DataFetchingEnvironment
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter
import org.springframework.graphql.execution.ErrorType
import org.springframework.stereotype.Component
import java.time.format.DateTimeParseException

@Component
class InvalidStayExceptionResolver : DataFetcherExceptionResolverAdapter() {

    override fun resolveToSingleError(
        exception: Throwable,
        environment: DataFetchingEnvironment,
    ): GraphQLError? =
        when (exception) {
            is InvalidStayException, is DateTimeParseException -> toBadRequest(exception, environment)
            else -> null
        }

    private fun toBadRequest(
        exception: Throwable,
        environment: DataFetchingEnvironment,
    ) = GraphqlErrorBuilder
        .newError(environment)
        .errorType(ErrorType.BAD_REQUEST)
        .message("Invalid stay: ${exception.message}")
        .build()
}
