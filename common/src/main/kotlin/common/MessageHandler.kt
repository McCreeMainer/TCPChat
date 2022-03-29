package common

import io.ktor.utils.io.*
import java.io.ByteArrayInputStream
import java.io.DataInputStream

open class MessageHandler(
    private val readChannel: ByteReadChannel,
    private val writeChannel: ByteWriteChannel
) {
    open suspend fun send(
        type: Short,
        user: String? = null,
        text: String? = null,
        fileName: String? = null,
        fileData: ByteArray? = null
    ) {
        send(Message(type, user, text, fileName, fileData))
    }

    open suspend fun send(message: Message) {
        send(message.toByteArray())
    }

    open suspend fun send(byteArray: ByteArray) {
        writeChannel.writeInt(byteArray.size)
        writeChannel.writeFully(byteArray)
        writeChannel.flush()
    }

    open suspend fun receive(messageHandler: (Message) -> Unit) {
        val byteArray = receiveByteArray()
        val message = Message.readMessage(
            DataInputStream(ByteArrayInputStream(byteArray))
        )
        messageHandler(message)
    }

    protected suspend fun receiveByteArray(): ByteArray {
        val size = readChannel.readInt()
        val temp = ByteArray(size)
        readChannel.readFully(temp, 0, size)
        return temp
    }
}