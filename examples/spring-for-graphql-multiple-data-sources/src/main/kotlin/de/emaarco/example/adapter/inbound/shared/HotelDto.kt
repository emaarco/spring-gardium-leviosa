package de.emaarco.example.adapter.inbound.shared

import de.emaarco.example.domain.Hotel
import de.emaarco.example.domain.Stay

data class HotelDto(
    val id: String,
    val name: String,
    val stars: Int,
    val stay: Stay,
) {
    companion object {
        fun from(
            hotel: Hotel,
            stay: Stay,
        ) = HotelDto(
            id = hotel.id.value,
            name = hotel.name.value,
            stars = hotel.stars,
            stay = stay,
        )
    }
}
