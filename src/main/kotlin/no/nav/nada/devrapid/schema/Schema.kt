package no.nav.nada.devrapid.schema

import com.sksamuel.avro4k.Avro
import com.sksamuel.avro4k.AvroDoc
import com.sksamuel.avro4k.AvroFixed
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Serializable
data class Target(val namespace: String, val zone: String, val environment: String)

val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssVV")

@Serializable
data class DevEvent(
    val nrn: NadaResourceNames,
    val application: String,
    val target: Target,
    @AvroFixed(20) @AvroDoc("ISO-8601 UTC timestamp")
    val timestamp: String,
    val additionalData: Map<String, String> = emptyMap(),
    val team: String
) {
    init {
        check(timestamp.length == 20) {
            "Timestamp should be in ISO8601 - Zulu time"
        }
        check(canParseTimestamp()) {"Timestamp should be in ISO8601 - Zulu time" }
    }

    fun canParseTimestamp(): Boolean {
        return try {
            val zd = ZonedDateTime.parse(timestamp, formatter)
            zd.zone == ZoneOffset.UTC
        } catch (e: Exception) {
            false
        }
    }
}
const val minLengthNrn = 10
@Serializable
data class NadaResourceNames(@AvroDoc("nrn:[team]:[app]:[id]") val id: String) {
    init {
        check(id.startsWith("nrn:")) { "IDs must start with `nrn:` prefix" }
        check(id.length > minLengthNrn) { "IDs must be longer than ${minLengthNrn} characters" }
        check(id.count { it == ':' } > 1) { "IDs must contain at least two colon separators" }
    }
}



fun main(args: Array<String>) {
    val schema = Avro.default.schema(DevEvent.serializer())
    println(schema.toString(true))
    val event = DevEvent(
        nrn = NadaResourceNames(id = "nrn:nada:push:test"),
        application = "nada-devrapid",
        target = Target(namespace = "q1", zone = "fss", environment = "preprod"),
        team = "NADA",
        timestamp = now()
    )
    val json = Json(JsonConfiguration.Stable.copy(strictMode = false))
    val readBack = json.parse(DevEvent.serializer(), json.stringify(DevEvent.serializer(), event))
    require(event.equals(readBack))
}

fun now(): String {
    return ZonedDateTime.now(ZoneOffset.UTC).format(formatter)
}