package de.emaarco.example.application.service

import de.emaarco.example.application.port.inbound.LoadHotelQuery
import de.emaarco.example.application.port.outbound.HotelContentPort
import de.emaarco.example.domain.HotelId
import org.springframework.stereotype.Service

@Service
class LoadHotelService(
    private val hotelContent: HotelContentPort,
) : LoadHotelQuery {
    override fun loadHotel(hotelId: HotelId) = hotelContent.findHotel(hotelId)
}
