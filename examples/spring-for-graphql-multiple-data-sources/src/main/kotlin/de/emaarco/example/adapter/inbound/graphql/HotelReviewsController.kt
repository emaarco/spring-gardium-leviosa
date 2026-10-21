package de.emaarco.example.adapter.inbound.graphql

import de.emaarco.example.adapter.inbound.shared.HotelDetailsDto
import de.emaarco.example.adapter.inbound.shared.RatingDto
import de.emaarco.example.adapter.inbound.shared.ReviewDto
import de.emaarco.example.application.port.inbound.LoadHotelReviewsQuery
import de.emaarco.example.domain.HotelId
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
@SchemaMapping(typeName = "HotelDetails")
class HotelReviewsController(
    private val query: LoadHotelReviewsQuery,
) {

    @SchemaMapping
    fun rating(hotel: HotelDetailsDto): RatingDto? = query.loadRating(HotelId(hotel.id))?.let { RatingDto.from(it) }

    @SchemaMapping
    fun reviews(
        hotel: HotelDetailsDto,
        @Argument limit: Int,
    ): List<ReviewDto> = query.loadLatestReviews(HotelId(hotel.id), limit).map { ReviewDto.from(it) }
}
