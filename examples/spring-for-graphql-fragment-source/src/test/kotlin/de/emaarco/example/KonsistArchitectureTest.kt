package de.emaarco.example

import de.emaarco.konsist.BasicCodingGuidelinesTest
import de.emaarco.konsist.HexagonalArchitectureTest
import org.junit.jupiter.api.Nested

class KonsistArchitectureTest {

    private val rootPackage = "de.emaarco.example"

    @Nested
    inner class Architecture : HexagonalArchitectureTest(rootPackage)

    @Nested
    inner class CodingGuidelines : BasicCodingGuidelinesTest(rootPackage)
}
