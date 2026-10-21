package de.emaarco.example.application.service

import de.emaarco.example.application.port.inbound.SearchHotelsQuery
import de.emaarco.example.application.port.outbound.HotelContentPort
import org.springframework.stereotype.Service

@Service
class SearchHotelsService(
    private val hotelContent: HotelContentPort,
) : SearchHotelsQuery {
    override fun searchHotels(city: String) = hotelContent.findHotelsIn(city)
}
