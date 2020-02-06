package no.nav.nada.devrapid.schema

import com.github.guepardoapps.kulid.ULID
import com.sksamuel.avro4k.Avro
import com.sksamuel.avro4k.AvroFixed
import com.sksamuel.avro4k.serializer.InstantSerializer
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class Target(val name: String)
@Serializable
data class Metadata(@Serializable(with=InstantSerializer::class) val receivedAt: Instant, @AvroFixed(26) val ulid: String = ULID.random())

@Serializable
data class DevEvent(val id: String,
                    val application: String,
                    @Serializable(with=InstantSerializer::class) val started_at: Instant,
                    val target: Target,
                    @Serializable(with=InstantSerializer::class) val timestamp: Instant = Instant.now(),
                    val details: Map<String, String> = emptyMap(),
                    val team: String,
                    val trigger: String,
                    val environment: String,
                    val metadata: Metadata)


fun main(args: Array<String>): Unit {
    val avro = Avro()
    val schema = Avro.default.schema(DevEvent.serializer())
    println(schema.toString(true))
    avro.toRecord(DevEvent.serializer(), DevEvent(id = "nrn:nada:push:${ULID.random()}", application = "nada-devrapid",
            started_at = Instant.now().minusSeconds(120),
            target = Target("Schema deploy"),
            team = "NADA",
            trigger = "Manual",
            environment = "dev",
            metadata = Metadata(receivedAt = Instant.now())
    ))
}