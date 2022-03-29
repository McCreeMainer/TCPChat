package common

import kotlinx.datetime.Instant
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.AbstractEncoder
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import java.io.DataOutput

@OptIn(ExperimentalSerializationApi::class)
class DataOutputEncoder(val output: DataOutput) : AbstractEncoder() {
    override val serializersModule: SerializersModule = EmptySerializersModule
    override fun encodeBoolean(value: Boolean) = output.writeByte(if (value) 1 else 0)
    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) = output.writeInt(index)
    override fun encodeChar(value: Char) = output.writeChar(value.code)
    override fun encodeString(value: String) = output.writeUTF(value)
    override fun encodeInt(value: Int) = output.writeInt(value)
    override fun encodeShort(value: Short) = output.writeShort(value.toInt())
    override fun encodeByte(value: Byte) = output.writeByte(value.toInt())
    private fun encodeBytes(value: ByteArray) = output.write(value)

    override fun beginCollection(descriptor: SerialDescriptor, collectionSize: Int): CompositeEncoder {
        encodeInt(collectionSize)
        return this
    }

    override fun encodeNull() = encodeBoolean(false)
    override fun encodeNotNullMark() = encodeBoolean(true)

    @Suppress("UNCHECKED_CAST")
    override fun <T> encodeSerializableValue(serializer: SerializationStrategy<T>, value: T) {
        when (serializer.descriptor) {
            serializer<ByteArray>().descriptor -> encodeByteArray(value as ByteArray)
            serializer<Instant>().descriptor -> encodeInstant(value as Instant)
            else -> super.encodeSerializableValue(serializer, value)
        }
    }

    private fun encodeByteArray(bytes: ByteArray) {
        encodeCompactSize(bytes.size)
        encodeBytes(bytes)
    }

    private fun encodeCompactSize(value: Int) {
        if (value < 0xff) {
            output.writeByte(value)
        } else {
            output.writeByte(0xff)
            output.writeInt(value)
        }
    }

    private fun encodeInstant(instant: Instant) {
        encodeString(instant.toString())
    }
}

val byteArraySerializer = serializer<ByteArray>()
val instantSerializer = serializer<Instant>()

fun <T> encodeTo(output: DataOutput, serializer: SerializationStrategy<T>, value: T) {
    val encoder = DataOutputEncoder(output)
    encoder.encodeSerializableValue(serializer, value)
}

inline fun <reified T> encodeTo(output: DataOutput, value: T) = encodeTo(output, serializer(), value)