package de.emaarco.example.domain

data class Hotel(
    val id: HotelId,
    val name: HotelName,
    val city: String,
    val stars: Int,
)
