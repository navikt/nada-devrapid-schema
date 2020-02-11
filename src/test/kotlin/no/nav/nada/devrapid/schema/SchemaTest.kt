package no.nav.nada.devrapid.schema

import com.sksamuel.avro4k.Avro
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.protobuf.ProtoBuf
import org.apache.avro.AvroRuntimeException
import org.apache.avro.generic.GenericData
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.stream.Stream

class SchemaTest {
    val now = now()
    val event = DevEvent(
        nrn = NadaResourceNames(id = "nrn:nada:push:test"),
        application = "nada-devrapid",
        target = Target(namespace = "q1", zone = "fss", environment = "preprod"),
        team = "NADA",
        timestamp = now
    )

    @Test
    fun shouldHandleRoundtripToAvro() {
        val record = Avro.default.toRecord(DevEvent.serializer(), event)
        val fromRecord = Avro.default.fromRecord(DevEvent.serializer(), record)
        assertThat(fromRecord).isEqualTo(event)
    }

    private val jsonSerializer = Json(JsonConfiguration.Stable.copy(strictMode = false))

    @Test
    fun shouldHandleRoundtripToJson() {
        val json = jsonSerializer
        val jsonEl = json.stringify(DevEvent.serializer(), event)
        val fromJson = json.parse(DevEvent.serializer(), jsonEl)
        assertThat(fromJson).isEqualTo(event)
    }

    @Test
    fun shouldHandleRoundtripToProtobuf() {
        val proto = ProtoBuf()
        val protoEl = proto.dump(DevEvent.serializer(), event)
        val fromProto = proto.load(DevEvent.serializer(), protoEl)
        assertThat(fromProto).isEqualTo(event)
    }

    @Test
    fun shouldHandleHandwrittenJson() {
        val jsonEl = """
            {
                "nrn": {
                    "id": "nrn:nada:push:test"
                },
                "application": "nada-devrapid",
                "target": {
                    "namespace": "q1",
                    "zone": "fss",
                    "environment": "preprod"
                },
                "team": "NADA",
                "timestamp": "$now"
            }
        """.trimIndent()
        val parsedEvent = jsonSerializer.parse(DevEvent.serializer(), jsonEl)
        assertThat(parsedEvent).isEqualTo(event)
    }

    @Test
    fun incorrectTimestampFormatShouldFail() {
        val jsonEl = """
            {
                "nrn": {
                    "id": "nrn:nada:push:test"
                },
                "application": "nada-devrapid",
                "target": {
                    "namespace": "q1",
                    "zone": "fss",
                    "environment": "preprod"
                },
                "team": "NADA",
                "timestamp": "2012-05-01T12:13:55+01:00"
            }
        """.trimIndent()
        assertThatThrownBy { jsonSerializer.parse(DevEvent.serializer(), jsonEl) }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessage("Timestamp should be in ISO8601 - Zulu time")
    }

    @ParameterizedTest
    @MethodSource("nrnFormats")
    fun `fails if nrn format is not correct`(nrn: String, expectedMessage: String) {
        val jsonEl = """
            {
                "nrn": {
                    "id": "$nrn"
                },
                "application": "nada-devrapid",
                "target": {
                    "namespace": "q1",
                    "zone": "fss",
                    "environment": "preprod"
                },
                "team": "NADA",
                "timestamp": "2012-05-01T12:13:55Z"
            }
        """.trimIndent()
        assertThatThrownBy { jsonSerializer.parse(DevEvent.serializer(), jsonEl) }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessage(expectedMessage)
    }

    @Test
    fun `timestamp without timezone gets rejected for json`() {
        val withoutTimezone = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

        val jsonEl = """
            {
                "nrn": {
                    "id": "nrn:nada:push:test"
                },
                "application": "nada-devrapid",
                "target": {
                    "namespace": "q1",
                    "zone": "fss",
                    "environment": "preprod"
                },
                "team": "NADA",
                "timestamp": "$withoutTimezone"
            }
        """.trimIndent()
        assertThrows<IllegalStateException> {
            val parsedEvent = jsonSerializer.parse(DevEvent.serializer(), jsonEl)
            assertThat(parsedEvent.timestamp).isEqualTo(now)
        }
    }

    @Test
    fun `timestamp without timezone gets rejected for avro`() {
        val withoutTimezone = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val schema = Avro.default.schema(DevEvent.serializer())
        val targetSchema = Avro.default.schema(Target.serializer())
        val nrnSchema = Avro.default.schema(NadaResourceNames.serializer())
        val target = GenericData.Record(targetSchema).apply {
            put("namespace", "q1")
            put("zone", "fss")
            put("environment", "preprod")
        }
        val nrn = GenericData.Record(nrnSchema).apply {
            put("id", "nrn:nada:push:test")
        }
        val record = GenericData.Record(schema).apply {
            put("nrn", nrn)
            put("application", "nada-devrapid")
            put("target", target)
            put("additionalData", emptyMap<Any, Any>())
            put("team", "NADA")
            put("timestamp", withoutTimezone)
        }
        assertThatThrownBy {
            Avro.default.fromRecord(DevEvent.serializer(), record)
        }.isInstanceOf(IllegalStateException::class.java)
    }


    @Test
    fun `should handle handwritten avro`() {
        val schema = Avro.default.schema(DevEvent.serializer())
        val targetSchema = Avro.default.schema(Target.serializer())
        val nrnSchema = Avro.default.schema(NadaResourceNames.serializer())
        val target = GenericData.Record(targetSchema).apply {
            put("namespace", "q1")
            put("zone", "fss")
            put("environment", "preprod")
        }
        val nrn = GenericData.Record(nrnSchema).apply {
            put("id", "nrn:nada:push:test")
        }
        val record = GenericData.Record(schema).apply {
            put("nrn", nrn)
            put("application", "nada-devrapid")
            put("target", target)
            put("additionalData", emptyMap<Any, Any>())
            put("team", "NADA")
            put("timestamp", now)
        }
        val fromRecord = Avro.default.fromRecord(DevEvent.serializer(), record)
        assertThat(fromRecord).isEqualTo(event)
    }

    companion object {
        @JvmStatic
        fun nrnFormats(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("nrn:nada", "IDs must be longer than 10 characters"),
                Arguments.of("test:nada", "IDs must start with `nrn:` prefix"),
                Arguments.of("nrn:dataplattform", "IDs must contain at least two colon separators")
            )
        }
    }
}
