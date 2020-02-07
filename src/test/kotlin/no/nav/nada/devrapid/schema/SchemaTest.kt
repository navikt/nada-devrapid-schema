package no.nav.nada.devrapid.schema

import com.sksamuel.avro4k.Avro
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.protobuf.ProtoBuf
import org.apache.avro.generic.GenericData
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SchemaTest {
    val now = now()
    val ulid = "12345678901234567890123456"
    val event = DevEvent(id = "nrn:nada:push:test",
            application = "nada-devrapid",
            target = Target(namespace = "q1", zone = "fss", environment = "preprod"),
            team = "NADA",
            metadata = Metadata(receivedAt = now, ulid = ulid),
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
                "id": "nrn:nada:push:test",
                "application": "nada-devrapid",
                "target": {
                    "namespace": "q1",
                    "zone": "fss",
                    "environment": "preprod"
                },
                "team": "NADA",
                "metadata": {
                    "receivedAt": "$now",
                    "ulid": "$ulid"
                },
                "timestamp": "$now"
            }
        """.trimIndent()
        val parsedEvent = jsonSerializer.parse(DevEvent.serializer(), jsonEl)
        assertThat(parsedEvent).isEqualTo(event)
    }

    @Test
    fun `should handle handwritten avro`() {
        val schema = Avro.default.schema(DevEvent.serializer())
        val targetSchema = Avro.default.schema(Target.serializer())
        val metadataSchema = Avro.default.schema(Metadata.serializer())
        val target = GenericData.Record(targetSchema).apply {
            put("namespace", "q1")
            put("zone", "fss")
            put("environment", "preprod")
        }
        val metadata = GenericData.Record(metadataSchema).apply {
            put("receivedAt", now)
            put("ulid", ulid)
        }
        val record = GenericData.Record(schema).apply {
            put("id", "nrn:nada:push:test")
            put("application", "nada-devrapid")
            put("target", target)
            put("additionalData", emptyMap<Any, Any>())
            put("metadata", metadata)
            put("team", "NADA")
            put("timestamp", now)
        }
        val fromRecord = Avro.default.fromRecord(DevEvent.serializer(), record)
        assertThat(fromRecord).isEqualTo(event)
    }

}
