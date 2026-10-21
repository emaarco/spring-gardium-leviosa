package de.emaarco.example.adapter.inbound.graphql

import de.emaarco.example.adapter.inbound.shared.HotelDetailsDto
import de.emaarco.example.application.port.inbound.LoadHotelQuery
import de.emaarco.example.domain.HotelId
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class HotelDetailsController(
    private val query: LoadHotelQuery,
) {

    private val log = KotlinLogging.logger {}

    @QueryMapping
    fun hotel(
        @Argument id: String,
    ): HotelDetailsDto? {
        log.debug { "Received graphql-request to load hotel $id" }
        return query.loadHotel(HotelId(id))?.let { HotelDetailsDto.from(it) }
    }
}
