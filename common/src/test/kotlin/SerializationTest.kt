import common.Message
import common.MessageType
import common.decodeFrom
import common.encodeTo
import kotlinx.datetime.Clock
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import kotlin.test.assertEquals

class SerializationTest {

    private val sampleMessage = Message(
        MessageType.MESSAGE,
        "sus",
        "amogus",
        "vent",
        byteArrayOf(0x02, 0x04, 0x34),
        Clock.System.now(),
    )

    private val ruMessage = Message(
        MessageType.MESSAGE,
        "Леша",
        "Ну как там с деньгами?",
        "С какими деньгами?",
        byteArrayOf(0x02, 0x04, 0x34),
        Clock.System.now(),
    )

    @Test
    fun `Test serialization and deserialization`() {
        val output = ByteArrayOutputStream()
        encodeTo(DataOutputStream(output), sampleMessage)
        val bytes = output.toByteArray()
        val input = ByteArrayInputStream(bytes)
        val obj = decodeFrom<Message>(DataInputStream(input))
        assertEquals(sampleMessage, obj)
    }

    @Test
    fun `Test Cyrillic encoding`() {
        assertEquals(
            ruMessage,
            Message.readMessage(
                DataInputStream(
                    ByteArrayInputStream(ruMessage.toByteArray())
                )
            )
        )
    }
}