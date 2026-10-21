package de.emaarco.example.domain

data class RoomOffer(
    val hotelId: HotelId,
    val roomType: RoomType,
    val pricePerNightInEuro: Int,
    val totalPriceInEuro: Int,
)
