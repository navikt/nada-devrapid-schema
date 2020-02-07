package no.nav.nada.devrapid.schema

import com.github.guepardoapps.kulid.ULID
import com.sksamuel.avro4k.Avro
import com.sksamuel.avro4k.AvroDoc
import com.sksamuel.avro4k.AvroFixed
import com.sksamuel.avro4k.serializer.InstantSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.protobuf.ProtoBuf
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Serializable
data class Target(val namespace: String, val zone: String, val environment: String)

val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssVV")
@Serializable
data class Metadata(@AvroFixed(20) @AvroDoc("ISO-8601 UTC timestamp")
                    val receivedAt: String,
                    @AvroFixed(26)
                    val ulid: String = ULID.random())

@Serializable
data class DevEvent(val id: String,
                    val application: String,
                    val target: Target,
                    @AvroFixed(20) @AvroDoc("ISO-8601 UTC timestamp")
                    val timestamp: String,
                    val additionalData: Map<String, String> = emptyMap(),
                    val team: String,
                    val metadata: Metadata)


fun main(args: Array<String>) {
    val schema = Avro.default.schema(DevEvent.serializer())
    println(schema.toString(true))
    val event = DevEvent(id = "nrn:nada:push:${ULID.random()}",
            application = "nada-devrapid",
            target = Target(namespace = "q1", zone = "fss", environment = "preprod"),
            team = "NADA",
            metadata = Metadata(receivedAt = now()),
            timestamp = now()
            )
    val proto = Json(JsonConfiguration.Stable.copy(strictMode = false))
    val readBack = proto.parse(DevEvent.serializer(), proto.stringify(DevEvent.serializer(), event))
    require(event.equals(readBack))
}

fun now(): String {
    return ZonedDateTime.now(ZoneOffset.UTC).format(formatter)
}