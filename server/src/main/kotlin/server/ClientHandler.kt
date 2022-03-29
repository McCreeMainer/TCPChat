package server

import common.BLockingMessageHandler
import common.Message
import common.MessageHandler
import common.MessageType
import io.ktor.network.sockets.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import java.net.SocketException

class ClientHandler(
    val socket: Socket,
    val mutex: Mutex?,
    val onAuthorization: (ClientHandler, String?) -> Unit,
    val onReceive: (Message) -> Unit,
    val onDisconnect: (ClientHandler) -> Unit,
) {
    var connected = false
    var username: String? = null
    private val readChannel = socket.openReadChannel()
    private val writeChannel = socket.openWriteChannel(autoFlush = false)
    private val messageHandler = if (mutex == null)
        MessageHandler(readChannel, writeChannel)
    else
        BLockingMessageHandler(mutex, readChannel, writeChannel)

    init {
        connected = true
        println("${socket.remoteAddress} connected")
        CoroutineScope(Dispatchers.Default).launch {
            try {
                while (connected) {
                    messageHandler.receive(::handleMessage)
                }
            } catch (e: SocketException) {
                onDisconnect(this@ClientHandler)
            }

            println("${socket.remoteAddress} disconnected")
        }
    }

    fun sendMessage(message: Message) {
        CoroutineScope(Dispatchers.IO).launch {
            messageHandler.send(message)
        }
    }

    private fun handleMessage(message: Message) {
        when (message.type) {
            MessageType.LOGIN_USER -> onAuthorization(this, message.text)
            MessageType.MESSAGE -> {
                if (username == null) {
                    sendMessage(Message(type = MessageType.LOGIN_REQUIRED))
                    return
                }

                message.user = username
                if (message.fileName != null && message.fileData != null) {
                    message.text = "Send file ${message.fileName}"
                } else {
                    message.fileName = null
                    message.fileData = null
                }

                onReceive(message)
            }
            MessageType.DISCONNECT -> disconnect()
            MessageType.ERROR -> { println("ERROR: Code ${message.text}") }
            else -> {}
        }
    }

    private fun disconnect() {
        connected = false
        onDisconnect(this)
    }
}
