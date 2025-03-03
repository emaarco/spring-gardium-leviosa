package de.emaarco.example

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.conditions.ArchConditions.resideInAnyPackage
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.stereotype.Service

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class ArchitectureTest(
    private val pathFromRoot: String,
) {

    private val allClasses: JavaClasses by lazy { ClassFileImporter().importPackages(pathFromRoot) }
    private val productionClasses: JavaClasses by lazy {
        ClassFileImporter().withImportOption(DoNotIncludeTests()).importPackages(pathFromRoot)
    }

    @Test
    fun `each class has a package declaration`() =
        check {
            classes().should().resideInAnyPackage("$pathFromRoot..")
        }

    @Test
    fun `classes are free of cycles`() =
        check {
            SlicesRuleDefinition
                .slices()
                .matching("${this.pathFromRoot}.(**)")
                .should()
                .beFreeOfCycles()
        }

    @Test
    fun `classes should comply to package structure`() =
        check {
            classes()
                .that()
                .resideInAPackage("${this.pathFromRoot}..")
                .should(resideInAnyPackage(*getAllAllowedPackages()))
                .because("Classes should comply with the specified package structure.")
        }

    @Test
    fun `services should not depend on other services`() =
        check(productionClasses) {
            noClasses()
                .that()
                .arePublic()
                .and()
                .resideInAPackage("..application.service..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage("..application.service..")
                .andShould()
                .dependOnClassesThat()
                .areAnnotatedWith(Service::class.java)
                .because("Services should not use other services because it would mix up isolated use-cases")
        }

    @Test
    fun `in-adapters should use ports to interact with services`() =
        check(productionClasses) {
            noClasses()
                .that()
                .resideInAPackage("..adapter.inbound..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage("..application.service..")
                .because("In-adapters should use ports to interact with services")
        }

    @Test
    fun `application services should use out-ports to interact with out-adapters`() =
        check(productionClasses) {
            noClasses()
                .that()
                .resideInAPackage("..application.service..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage("..adapter..")
                .because("Application services should use out-ports to interact with out-adapters")
        }

    @Test
    fun `application services should not use each other`() =
        check(productionClasses) {
            noClasses()
                .that()
                .resideInAPackage("..application.service..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage("..application.service..")
                .because("Application services should not depend each other")
        }

    @Test
    fun `domain classes should not be defined as beans`() =
        check(productionClasses) {
            noClasses()
                .that()
                .resideInAPackage("..domain..")
                .should()
                .beAnnotatedWith("org.springframework.stereotype.Component")
                .orShould()
                .beAnnotatedWith("org.springframework.stereotype.Service")
                .because("Domain classes should not be defined as beans")
        }

    private fun check(
        classes: JavaClasses? = allClasses,
        rule: () -> ArchRule,
    ) = rule().check(classes)

    private fun getAllAllowedPackages(): Array<String> {
        val allowedPackages = mutableListOf<String>()
        allowedPackages.add(this.pathFromRoot)
        packageStructure.forEach { allowedPackages.add("${this.pathFromRoot}.$it..") }
        return allowedPackages.toTypedArray()
    }

    /**
     * Defines the top-level packages that are allowed in a hexagonal architecture
     */
    private val packageStructure =
        listOf(
            "adapter.inbound",
            "adapter.outbound",
            "application.port.inbound",
            "application.port.outbound",
            "application.service",
            "domain",
        )
}
