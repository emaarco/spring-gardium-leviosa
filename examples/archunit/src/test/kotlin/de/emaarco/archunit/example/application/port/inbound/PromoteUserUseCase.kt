package de.emaarco.archunit.example.application.port.inbound

/** Inbound port of the third example service — a second state-changing use case with its own command. */
interface PromoteUserUseCase {
    fun promote(command: Command)

    data class Command(
        val name: String,
    )
}
