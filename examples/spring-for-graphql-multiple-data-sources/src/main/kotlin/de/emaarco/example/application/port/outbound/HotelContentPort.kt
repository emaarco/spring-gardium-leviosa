package de.emaarco.example.application.port.outbound

import de.emaarco.example.domain.Hotel
import de.emaarco.example.domain.HotelId

interface HotelContentPort {
    fun findHotelsIn(city: String): List<Hotel>

    fun findHotel(hotelId: HotelId): Hotel?
}
