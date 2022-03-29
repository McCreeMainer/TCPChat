package common

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import java.io.ByteArrayOutputStream
import java.io.DataInput
import java.io.DataOutputStream

@Serializable
data class Message(
    var type: Short,
    var user: String? = null,
    var text: String? = null,
    var fileName: String? = null,
    var fileData: ByteArray? = null,
    var time: Instant = Clock.System.now(),
) {
    companion object {
        fun readMessage(input: DataInput): Message = decodeFrom(input)
    }

    fun toByteArray(): ByteArray {
        val output = ByteArrayOutputStream()
        encodeTo(DataOutputStream(output), this)
        return output.toByteArray()
    }
}
