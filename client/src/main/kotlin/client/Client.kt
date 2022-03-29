package client

import common.ErrorCode
import common.Message
import common.MessageHandler
import common.MessageType
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.io.File
import java.net.ConnectException
import java.net.InetSocketAddress
import java.net.SocketException


class Client(val address: String, val port: Int, val timeZone: TimeZone? = null) {

    var connected = true
    var authorized = false
    private lateinit var socket: Socket
    private lateinit var readChannel: ByteReadChannel
    private lateinit var writeChannel: ByteWriteChannel
    private lateinit var messageHandler: MessageHandler

    init {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                socket = aSocket(ActorSelectorManager(Dispatchers.IO)).tcp()
                    .connect(InetSocketAddress(address, port))
                readChannel = socket.openReadChannel()
                writeChannel = socket.openWriteChannel(autoFlush = false)
                messageHandler = MessageHandler(readChannel, writeChannel)

                println("Client connected ${socket.localAddress}")
                println("Timezone: ${timeZone?.id ?: TimeZone.currentSystemDefault().id}")
                println("Available commands:")
                println(HELP_MESSAGE)

                while (connected) {
                    messageHandler.receive(::handleMessage)
                }

                socket.dispose()
            } catch (_: ConnectException) {
                println("Server is not available")
                connected = false
            } catch (_: SocketException) {
                println("Server is down")
                connected = false
            }
        }
    }

    fun login(username: String) {
        sendMessage(
            Message(
                type = MessageType.LOGIN_USER,
                text = username
            )
        )
    }

    fun sendTextMessage(text: String) {
        sendMessage(
            Message(
                type = MessageType.MESSAGE,
                text = text
            )
        )
    }

    fun sendFileMessage(fileName: String) {
        val file = File(fileName)
        if (file.exists()) {
            val data = file.readBytes()
            sendMessage(
                Message(
                    type = MessageType.MESSAGE,
                    fileName = file.name,
                    fileData = data
                )
            )
        } else {
            println("File ${file.absolutePath} is not exists")
        }
    }

    fun disconnect() {
        println("Disconnecting...")
        sendMessage(Message(type = MessageType.DISCONNECT))
    }

    fun sendMessage(message: Message) {
        if (!connected) return
        CoroutineScope(Dispatchers.IO).launch {
            messageHandler.send(message)
        }
    }

    private fun handleMessage(message: Message) {
        when (message.type) {
            MessageType.LOGIN_REQUIRED -> {
                println("Auth required")
            }
            MessageType.LOGIN_SUCCESSFUL -> {
                println("Auth successful")
                authorized = true
            }
            MessageType.MESSAGE -> {
                receiveMessage(message)
            }
            MessageType.DISCONNECT -> {
                disconnect()
            }
            MessageType.ERROR -> {
                printError(message.text?.toInt() ?: -1)
            }
            else -> {}
        }
    }

    private fun receiveMessage(message: Message) {
        print(formatDateTime(message.time.toLocalDateTime(timeZone ?: TimeZone.currentSystemDefault())))
        println(" ${message.user}: ${message.text}")
        message.fileName?.let { fileName ->
            message.fileData?.let { fileData ->
                saveFile(fileName, fileData)
            }
        }
    }

    private fun saveFile(fileName: String, data: ByteArray) {
        CoroutineScope(Dispatchers.IO).launch {
            println("Downloading file $fileName")

            var fileIndex = 0
            val path = "$FILE_REPO\\$fileName"
            var file = File(path)

            while (file.exists()) {
                val title = "${file.nameWithoutExtension} (${++fileIndex}).${file.extension}"
                file = File("${file.parent}/$title")
            }

            file.writeBytes(data)

            println("File saved at ${file.absolutePath}")
        }
    }

    private fun printError(code: Int) {
        val error = when (code) {
            ErrorCode.USER_BLANK -> "Incorrect username"
            ErrorCode.USER_ALREADY_EXIST -> "This username is already taken"
            ErrorCode.USER_LOGGED_IN -> "You have been already authorized"
            else -> "Unknown error"
        }
        println("ERROR: $error")
    }

    private fun formatDateTime(dateTime: LocalDateTime): String {
        return "[${dateTime.dayOfMonth}.${dateTime.monthNumber}.${dateTime.year} ${dateTime.hour}:${dateTime.minute}:${dateTime.second}]"
    }
}