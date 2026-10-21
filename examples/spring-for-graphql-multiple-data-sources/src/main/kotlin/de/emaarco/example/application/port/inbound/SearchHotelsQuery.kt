package de.emaarco.example.application.port.inbound

import de.emaarco.example.domain.Hotel

interface SearchHotelsQuery {
    fun searchHotels(city: String): List<Hotel>
}
