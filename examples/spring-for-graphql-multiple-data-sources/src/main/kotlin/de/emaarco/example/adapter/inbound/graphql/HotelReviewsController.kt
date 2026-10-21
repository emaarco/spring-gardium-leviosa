package de.emaarco.example.adapter.inbound.graphql

import de.emaarco.example.adapter.inbound.shared.HotelDetailsDto
import de.emaarco.example.adapter.inbound.shared.RatingDto
import de.emaarco.example.adapter.inbound.shared.ReviewDto
import de.emaarco.example.application.port.inbound.LoadHotelReviewsQuery
import de.emaarco.example.domain.HotelId
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
@SchemaMapping(typeName = "HotelDetails")
class HotelReviewsController(
    private val query: LoadHotelReviewsQuery,
) {

    private val log = KotlinLogging.logger {}

    @SchemaMapping
    fun rating(hotel: HotelDetailsDto): RatingDto? {
        log.debug { "Resolving rating of hotel ${hotel.id}" }
        val rating = query.loadRating(HotelId(hotel.id))
        return rating?.let { RatingDto.from(it) }
    }

    @SchemaMapping
    fun reviews(
        hotel: HotelDetailsDto,
        @Argument limit: Int,
    ): List<ReviewDto> {
        log.debug { "Resolving the latest $limit reviews of hotel ${hotel.id}" }
        val reviews = query.loadLatestReviews(HotelId(hotel.id), limit)
        return reviews.map { ReviewDto.from(it) }
    }
}
