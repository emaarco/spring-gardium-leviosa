package de.emaarco.archunit.example.application.service

import de.emaarco.archunit.example.application.port.inbound.RegisterUserUseCase
import de.emaarco.archunit.example.application.port.outbound.UserRepository

/**
 * POSITIVE case: implements its own use case and never calls another one — allowed.
 *
 * It only reaches out through an *outbound* port and reads the getters of its own nested
 * [RegisterUserUseCase.Command]. The rule forbids accessing inbound *port interfaces* (not their
 * command/query data types), and implementing [RegisterUserUseCase] is inheritance rather than
 * access, so this class is left alone.
 */
class RegisterUserService(
    private val userRepository: UserRepository,
) : RegisterUserUseCase {
    override fun register(command: RegisterUserUseCase.Command) {
        userRepository.save(command.name)
    }
}
