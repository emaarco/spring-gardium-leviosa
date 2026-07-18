package de.emaarco.example

import de.emaarco.archunit.BasicCodingGuidelinesTest
import de.emaarco.archunit.HexagonalArchitectureTest
import de.emaarco.archunit.NamingConventionArchitectureTest
import org.junit.jupiter.api.Nested

/**
 * Demonstrates the standalone [`archunit`] module used on its own (bytecode-based rules only).
 */
class ArchUnitArchitectureTest {

    private val rootPackage = "de.emaarco.example"

    @Nested
    inner class Architecture : HexagonalArchitectureTest(rootPackage)

    @Nested
    inner class Naming : NamingConventionArchitectureTest(rootPackage)

    @Nested
    inner class CodingGuidelines : BasicCodingGuidelinesTest(rootPackage)
}
