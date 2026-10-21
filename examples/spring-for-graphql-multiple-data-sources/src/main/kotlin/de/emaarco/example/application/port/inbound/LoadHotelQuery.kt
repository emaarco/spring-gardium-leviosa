package de.emaarco.example.application.port.inbound

import de.emaarco.example.domain.Hotel
import de.emaarco.example.domain.HotelId

interface LoadHotelQuery {
    fun loadHotel(hotelId: HotelId): Hotel?
}
