package de.emaarco.example

import de.emaarco.archunit.BasicCodingGuidelinesTest
import de.emaarco.archunit.HexagonalArchitectureTest
import org.junit.jupiter.api.Nested

class FragmentSourceExampleArchitectureTest {

    private val rootPackage = "de.emaarco.example"

    @Nested
    inner class Architecture : HexagonalArchitectureTest(rootPackage)

    @Nested
    inner class CodingGuidelines : BasicCodingGuidelinesTest(rootPackage)
}
