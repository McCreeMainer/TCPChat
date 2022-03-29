package common

import io.ktor.utils.io.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.ByteArrayInputStream
import java.io.DataInputStream

class BLockingMessageHandler(
    private val mutex: Mutex,
    readChannel: ByteReadChannel,
    writeChannel: ByteWriteChannel
): MessageHandler(readChannel, writeChannel) {

    override suspend fun send(
        type: Short,
        user: String?,
        text: String?,
        fileName: String?,
        fileData: ByteArray?
    ) {
        send(Message(type, user, text, fileName, fileData))
    }

    override suspend fun send(message: Message) {
        send(message.toByteArray())
    }

    override suspend fun send(byteArray: ByteArray) {
        mutex.withLock {
            super.send(byteArray)
        }
    }

    override suspend fun receive(messageHandler: (Message) -> Unit) {
        val byteArray = receiveByteArray()
        mutex.withLock {
            val message = Message.readMessage(
                DataInputStream(ByteArrayInputStream(byteArray))
            )
            messageHandler(message)
        }
    }
}