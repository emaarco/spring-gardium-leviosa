package de.emaarco.archunit.example.application.port.outbound

/** Outbound port of the third example service — a service is allowed to access this. */
interface UserRepository {
    fun save(name: String)
}
