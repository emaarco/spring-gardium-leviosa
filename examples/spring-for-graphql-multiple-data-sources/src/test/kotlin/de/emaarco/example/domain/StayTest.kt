package de.emaarco.example.domain

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatIllegalArgumentException
import org.junit.jupiter.api.Test
import java.time.LocalDate

class StayTest {

    @Test
    fun `nights include the check-in day but not the check-out day`() {
        val stay = Stay(LocalDate.of(2026, 12, 11), LocalDate.of(2026, 12, 13))

        assertThat(stay.nights()).containsExactly(LocalDate.of(2026, 12, 11), LocalDate.of(2026, 12, 12))
    }

    @Test
    fun `check-out must be after check-in`() {
        assertThatIllegalArgumentException().isThrownBy {
            Stay(LocalDate.of(2026, 12, 11), LocalDate.of(2026, 12, 11))
        }
    }
}
