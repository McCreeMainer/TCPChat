package server

import common.ErrorCode
import common.Message
import common.MessageType
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import java.net.InetSocketAddress
import java.net.SocketException

class Server(val address: String, val port: Int, val blocking: Boolean) {

    var running = true
    val mutex = Mutex(locked = false)
    val users = mutableListOf<ClientHandler>()

    init {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val server = aSocket(ActorSelectorManager(Dispatchers.IO)).tcp()
                    .bind(InetSocketAddress(address, port))

                println("Server running at ${server.localAddress}")
                print("Socket mode: ")
                if (blocking)
                    println("Blocking")
                else
                    println("Non-blocking")
                println("Available commands:")
                println(HELP_MESSAGE)

                while (running) {
                    acceptClient(server)
                }

                server.dispose()
            } catch (_ : SocketException) {
                println("Something wrong with socket settings")
                running = false
            }
        }
    }

    fun broadcast(text: String) {
        val message = Message(
            type = MessageType.MESSAGE,
            user = "BROADCAST",
            text = text
        )

        users.forEach {
            it.sendMessage(message)
        }
    }

    fun stop() {
        println("Server is shutting down...")

        val message = Message(type = MessageType.DISCONNECT)
        users.forEach {
            it.sendMessage(message)
        }

        running = false
    }

    private suspend fun acceptClient(serverSocket: ServerSocket) {
        val clientSocket = serverSocket.accept()
        ClientHandler(
            socket = clientSocket,
            mutex = if (blocking) mutex else null,
            onAuthorization = ::authorizeUser,
            onReceive = ::resendMessage,
            onDisconnect = ::disconnectUser
        )
    }

    private fun authorizeUser(client: ClientHandler, username: String?) {
        val message = when {
            client.username != null ->
                Message(type = MessageType.ERROR, text = ErrorCode.USER_LOGGED_IN.toString())
            username.isNullOrBlank() ->
                Message(type = MessageType.ERROR, text = ErrorCode.USER_BLANK.toString())
            username == "BROADCAST" || users.any { it.username.equals(username) } ->
                Message(type = MessageType.ERROR, text = ErrorCode.USER_ALREADY_EXIST.toString())
            else -> {
                client.username = username
                users.add(client)
                println("${client.socket.remoteAddress} is logged in as $username")
                Message(type = MessageType.LOGIN_SUCCESSFUL)
            }
        }

        client.sendMessage(message)
    }

    private fun resendMessage(message: Message) {
        users.forEach {
            if (it.username == message.user) {
                it.sendMessage(message.copy(fileName = null, fileData = null))
            } else {
                it.sendMessage(message)
            }
        }
    }

    private fun disconnectUser(client: ClientHandler) {
        users.remove(client)
    }
}