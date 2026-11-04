package de.emaarco.example

import org.awaitility.kotlin.atMost
import org.awaitility.kotlin.await
import org.awaitility.kotlin.until
import org.springframework.test.util.TestSocketUtils
import org.springframework.web.client.RestClient
import java.io.File
import java.time.Duration
import java.util.concurrent.TimeUnit.SECONDS

class EventRelaySidecarProcess(
    podName: String,
    applicationPort: Int,
    kafkaBrokers: String,
) : AutoCloseable {

    private val port = TestSocketUtils.findAvailableTcpPort()

    val url = "http://localhost:$port"

    private val log = File("build/event-relay-sidecar-$podName.log")

    private val process =
        ProcessBuilder(
            ProcessHandle
                .current()
                .info()
                .command()
                .orElseThrow(),
            "-jar",
            System.getProperty("event-relay-sidecar.jar"),
            "--server.port=$port",
            "--pod.name=$podName",
            "--spring.kafka.bootstrap-servers=$kafkaBrokers",
            "--relay.topic=order-status-changes",
            "--relay.delivery-url=http://localhost:$applicationPort/internal/order-status-changes",
        ).redirectErrorStream(true)
            .redirectOutput(log)
            .start()

    fun awaitReadiness() {
        await atMost Duration.ofSeconds(60) until {
            check(process.isAlive) { "Event relay sidecar stopped, see ${log.absolutePath}" }
            isReady()
        }
    }

    override fun close() {
        process.destroy()
        process.waitFor(10, SECONDS)
    }

    private fun isReady() =
        runCatching {
            RestClient
                .create(url)
                .get()
                .uri("/actuator/health/readiness")
                .retrieve()
                .toBodilessEntity()
        }.isSuccess
}
