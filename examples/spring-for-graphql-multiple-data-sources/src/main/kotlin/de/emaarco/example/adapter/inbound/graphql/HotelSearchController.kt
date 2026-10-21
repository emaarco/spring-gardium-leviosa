package de.emaarco.example.adapter.inbound.graphql

import de.emaarco.example.adapter.inbound.shared.HotelDto
import de.emaarco.example.application.port.inbound.SearchHotelsQuery
import de.emaarco.example.domain.Stay
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller
import java.time.LocalDate

@Controller
class HotelSearchController(
    private val query: SearchHotelsQuery,
) {

    private val log = KotlinLogging.logger {}

    @QueryMapping
    fun hotelSearch(
        @Argument city: String,
        @Argument checkIn: String,
        @Argument checkOut: String,
    ): List<HotelDto> {
        log.debug { "Received graphql-request to search hotels in $city" }
        val stay = Stay(LocalDate.parse(checkIn), LocalDate.parse(checkOut))
        return query.searchHotels(city).map { HotelDto.from(it, stay) }
    }
}
