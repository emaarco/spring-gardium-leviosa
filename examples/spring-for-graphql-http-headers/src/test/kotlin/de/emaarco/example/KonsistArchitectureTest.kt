package de.emaarco.example

import de.emaarco.konsist.BasicCodingGuidelinesTest
import de.emaarco.konsist.HexagonalArchitectureTest
import de.emaarco.konsist.NamingConventionArchitectureTest
import org.junit.jupiter.api.Nested

/**
 * Demonstrates the standalone [`konsist`] module used on its own (source-based rules only).
 */
class KonsistArchitectureTest {

    private val rootPackage = "de.emaarco.example"

    @Nested
    inner class Architecture : HexagonalArchitectureTest(rootPackage)

    @Nested
    inner class Naming : NamingConventionArchitectureTest(rootPackage)

    @Nested
    inner class CodingGuidelines : BasicCodingGuidelinesTest(rootPackage)
}
