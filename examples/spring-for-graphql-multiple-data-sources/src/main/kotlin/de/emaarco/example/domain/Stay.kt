package de.emaarco.example.domain

import java.time.LocalDate

data class Stay(
    val checkIn: LocalDate,
    val checkOut: LocalDate,
) {
    init {
        if (checkIn >= checkOut) throw InvalidStayException("Check-in ($checkIn) must be before check-out ($checkOut)")
    }

    fun nights(): List<LocalDate> = checkIn.datesUntil(checkOut).toList()
}
