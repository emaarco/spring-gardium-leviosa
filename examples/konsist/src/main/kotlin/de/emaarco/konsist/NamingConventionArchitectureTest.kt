package de.emaarco.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

/**
 * Naming conventions per hexagonal layer — the Konsist (source-based) counterpart to the ArchUnit
 * `NamingConventionArchitectureTest`. It reads the `.kt` declarations directly instead of the
 * compiled bytecode.
 *
 * Each rule lists the class-name suffixes allowed in a given package together with a short rationale
 * via [AllowedSuffix], so the file doubles as living documentation. Packages a given service does not
 * have are skipped (empty scope), so the same rule set fits every service.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class NamingConventionArchitectureTest(
    val rootPackage: String,
) {

    @Nested
    inner class Ports {

        @Test
        fun `inbound ports are use-cases or queries`() {
            checkNaming(
                packagePattern = "$rootPackage.application.port.inbound..",
                allowedSuffixes =
                    listOf(
                        AllowedSuffix("UseCase", "inbound port for a state-changing operation"),
                        AllowedSuffix("Query", "inbound port for a read-only operation"),
                    ),
            )
        }

        @Test
        fun `outbound ports are ports or repositories`() {
            checkNaming(
                packagePattern = "$rootPackage.application.port.outbound..",
                allowedSuffixes =
                    listOf(
                        AllowedSuffix("Port", "generic outbound port delegating to infrastructure"),
                        AllowedSuffix("Repository", "outbound port for persistence access"),
                    ),
            )
        }
    }

    @Nested
    inner class Application {

        @Test
        fun `application services are services`() {
            checkNaming(
                packagePattern = "$rootPackage.application.service..",
                allowedSuffixes =
                    listOf(
                        AllowedSuffix("Service", "orchestrates a use case and owns the transaction boundary"),
                        AllowedSuffix("Configuration", "Spring configuration for the application layer"),
                    ),
            )
        }
    }

    @Nested
    inner class InboundAdapters {

        @Test
        fun `graphql adapters follow naming conventions`() {
            checkNaming(
                packagePattern = "$rootPackage.adapter.inbound.graphql..",
                allowedSuffixes =
                    listOf(
                        AllowedSuffix("Controller", "GraphQL resolver, aligned with Spring for GraphQL"),
                        AllowedSuffix("DataLoader", "batched field resolver following the DataLoader pattern"),
                        AllowedSuffix("Configuration", "Spring configuration for the GraphQL adapter"),
                        AllowedSuffix("Interceptor", "WebGraphQlInterceptor enriching the request context"),
                        AllowedSuffix("Resolver", "argument resolver for controller method parameters"),
                        AllowedSuffix("Mapper", "translates between GraphQL DTOs and domain types"),
                    ),
            )
        }

        @Test
        fun `rest adapters follow naming conventions`() {
            checkNaming(
                packagePattern = "$rootPackage.adapter.inbound.rest..",
                allowedSuffixes =
                    listOf(
                        AllowedSuffix("Controller", "Spring MVC REST controller"),
                        AllowedSuffix("Dto", "REST response type outside the domain model"),
                        AllowedSuffix("Input", "REST request type"),
                        AllowedSuffix("Mapper", "translates between REST DTOs and domain types"),
                    ),
            )
        }

        @Test
        fun `shared inbound adapter code follows naming conventions`() {
            checkNaming(
                packagePattern = "$rootPackage.adapter.inbound.shared..",
                allowedSuffixes =
                    listOf(
                        AllowedSuffix("Dto", "response type shared across inbound adapters"),
                        AllowedSuffix("Input", "request type shared across inbound adapters"),
                        AllowedSuffix("RequestHeaders", "holder for custom request-header names"),
                        AllowedSuffix("Mapper", "shared boundary mapper"),
                    ),
            )
        }

        @Test
        fun `spring inbound adapters follow naming conventions`() {
            checkNaming(
                packagePattern = "$rootPackage.adapter.inbound.spring..",
                allowedSuffixes =
                    listOf(
                        AllowedSuffix("DataSink", "Spring lifecycle hook feeding data into a use case"),
                    ),
            )
        }
    }

    @Nested
    inner class OutboundAdapters {

        @Test
        fun `outbound adapters follow naming conventions`() {
            checkNaming(
                packagePattern = "$rootPackage.adapter.outbound..",
                allowedSuffixes =
                    listOf(
                        AllowedSuffix("PersistenceAdapter", "primary outbound adapter implementing out-ports"),
                        AllowedSuffix("Adapter", "outbound adapter adapting to infrastructure"),
                        AllowedSuffix("Mapper", "translates between infrastructure types and domain objects"),
                    ),
            )
        }
    }

    private fun checkNaming(
        packagePattern: String,
        allowedSuffixes: List<AllowedSuffix>,
    ) {
        require(allowedSuffixes.isNotEmpty()) { "allowedSuffixes must not be empty" }
        // scopeFromProduction() excludes test sources — test classes follow their own conventions
        // (e.g. `*Test`, `*TestConfiguration`) that must not leak into the production naming rules.
        val declarations =
            Konsist
                .scopeFromProduction()
                .classesAndInterfacesAndObjects(includeNested = false, includeLocal = false)
                .filter { it.resideInPackage(packagePattern) }
        if (declarations.isEmpty()) return
        declarations.assertTrue { declaration ->
            allowedSuffixes.any { declaration.hasNameEndingWith(it.suffix) }
        }
    }

    /**
     * An allowed class-name suffix together with a short rationale.
     * The [reason] documents *why* a suffix is allowed; it is not part of Konsist's failure message.
     */
    data class AllowedSuffix(
        val suffix: String,
        val reason: String,
    )
}
