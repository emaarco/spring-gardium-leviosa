package de.emaarco.example.adapter.outbound

import de.emaarco.example.domain.HotelId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class HotelContentAdapterTest {

    private val adapter = HotelContentAdapter()

    @Test
    fun `finds the hotels of a city sorted by name`() {
        val hotels = adapter.findHotelsIn("münchen")

        assertThat(hotels.map { it.name.value })
            .containsExactly("Hotel Bayerischer Hof", "Hotel Isarblick", "Pension am Viktualienmarkt")
    }

    @Test
    fun `unknown city has no hotels`() {
        assertThat(adapter.findHotelsIn("Atlantis")).isEmpty()
    }

    @Test
    fun `finds a single hotel by id`() {
        assertThat(adapter.findHotel(HotelId("muc-isarblick"))?.name?.value).isEqualTo("Hotel Isarblick")
    }

    @Test
    fun `unknown hotel id has no hotel`() {
        assertThat(adapter.findHotel(HotelId("unknown"))).isNull()
    }
}
