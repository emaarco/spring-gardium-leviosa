package de.emaarco.example.adapter.outbound

import de.emaarco.example.application.port.outbound.HotelContentPort
import de.emaarco.example.domain.Hotel
import de.emaarco.example.domain.HotelId
import de.emaarco.example.domain.HotelName
import org.springframework.stereotype.Component

@Component
class HotelContentAdapter : HotelContentPort {

    private val hotels =
        listOf(
            Hotel(HotelId("muc-pension-viktualienmarkt"), HotelName("Pension am Viktualienmarkt"), "München", 2),
            Hotel(HotelId("muc-bayerischer-hof"), HotelName("Hotel Bayerischer Hof"), "München", 5),
            Hotel(HotelId("muc-isarblick"), HotelName("Hotel Isarblick"), "München", 3),
            Hotel(HotelId("ham-elbufer"), HotelName("Hotel Elbufer"), "Hamburg", 4),
        )

    override fun findHotelsIn(city: String) =
        hotels
            .filter { it.city.equals(city, ignoreCase = true) }
            .sortedBy { it.name.value }
}
