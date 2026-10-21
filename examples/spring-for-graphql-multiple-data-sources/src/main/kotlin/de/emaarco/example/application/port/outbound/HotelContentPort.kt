package de.emaarco.example.application.port.outbound

import de.emaarco.example.domain.Hotel

interface HotelContentPort {
    fun findHotelsIn(city: String): List<Hotel>
}
