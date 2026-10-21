package de.emaarco.example.application.port.inbound

import de.emaarco.example.domain.HotelId
import de.emaarco.example.domain.Rating
import de.emaarco.example.domain.Review

interface LoadHotelReviewsQuery {
    fun loadRating(hotelId: HotelId): Rating?

    fun loadLatestReviews(
        hotelId: HotelId,
        limit: Int,
    ): List<Review>
}
