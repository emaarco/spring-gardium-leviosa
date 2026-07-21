package de.emaarco.archunit

import com.tngtech.archunit.core.domain.JavaClass.Predicates.INTERFACES
import com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Demonstrates that the `application service should not use any inbound port` rule from
 * [HexagonalArchitectureTest] actually fires — it is not enough that the two clean example services
 * pass; we want a service that *provably* violates the rule.
 *
 * The fixture under `de.emaarco.archunit.example` is a tiny third example service:
 * - `RegisterUserService` implements its own use case and only reads its own nested command -> allowed
 * - `PromoteUserService`  implements its own use case but *calls* another use case           -> rejected
 *
 * The rule below mirrors the production rule verbatim (same `accessClassesThat` predicate that
 * targets only the inbound *port interfaces*, not their command/query data types).
 */
class ServiceInboundPortAccessTest {

    private val fixture = ClassFileImporter().importPackages("de.emaarco.archunit.example")

    private val rule =
        ArchRuleDefinition
            .noClasses()
            .that()
            .resideInAPackage("..application.service..")
            .should()
            .accessClassesThat(
                resideInAPackage("..application.port.inbound..").and(INTERFACES),
            )

    @Test
    fun `rejects a service that calls another use-case`() {
        val result = rule.evaluate(fixture)
        val details = result.failureReport.details.joinToString(separator = "\n")
        assertThat(result.hasViolation()).isTrue()
        assertThat(details).contains("PromoteUserService")
    }

    @Test
    fun `accepts a service that only implements its own use-case`() {
        val result = rule.evaluate(fixture)
        val details = result.failureReport.details.joinToString(separator = "\n")
        assertThat(details).doesNotContain("RegisterUserService")
    }
}
