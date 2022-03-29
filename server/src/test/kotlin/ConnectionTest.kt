import client.Client
import common.Message
import common.MessageHandler
import common.MessageType
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import org.junit.Test
import server.ADDRESS
import server.PORT
import server.Server
import java.net.InetSocketAddress
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ConnectionTest {

    @Test
    fun testUTF8Handling() {
        val server = Server(ADDRESS, PORT, false)
        val client = Client(ADDRESS, PORT)

        runBlocking {
            val read = awaitAll(
                server.serverHandleUTF8(this),
                client.clientHandleUTF8(this)
            )
            val serverRead = read[0]
            val clientRead = read[1]

            assertNotNull(serverRead)
            assertNotNull(clientRead)
            assertEquals(serverRead, clientRead)
        }
    }

    private suspend fun Server.serverHandleUTF8(scope: CoroutineScope): Deferred<String?> {
        return scope.async(Dispatchers.Default) {
            val serverSocket = aSocket(ActorSelectorManager(Dispatchers.IO)).tcp()
                .bind(InetSocketAddress(address, port))
            val clientSocket = serverSocket.accept()
            val input = clientSocket.openReadChannel()
            val output = clientSocket.openWriteChannel(autoFlush = true)

            val line = input.readUTF8Line()
            output.writeFully(line?.toByteArray() ?: byteArrayOf())
            clientSocket.close()
            return@async line
        }
    }

    private suspend fun Client.clientHandleUTF8(scope: CoroutineScope): Deferred<String?> {
        return scope.async(Dispatchers.Default) {
            val clientSocket = aSocket(ActorSelectorManager(Dispatchers.IO)).tcp()
                .connect(InetSocketAddress(address, port))
            val input = clientSocket.openReadChannel()
            val output = clientSocket.openWriteChannel(autoFlush = true)

            val line = "test\n"
            output.writeFully(line.toByteArray())
            val response = input.readUTF8Line()
            clientSocket.close()
            return@async response
        }
    }

    val message = Message(
        type = MessageType.MESSAGE,
        user = "Amogus",
        text = "SUS",
        fileName = "Vent",
        fileData = byteArrayOf(0xE5.toByte(), 0x1A.toByte(), 0x4C.toByte())
    )

    @Test
    fun testMessageHandling() {
        val server = Server(ADDRESS, PORT, false)
        val client = Client(ADDRESS, PORT)

        runBlocking {
            val read = awaitAll(
                server.serverHandleMessage(this),
                client.clientHandleMessage(this)
            )
            val serverRead = read[0]
            val clientRead = read[1]

            assertNotNull(serverRead)
            assertNotNull(clientRead)
            assertEquals(serverRead, clientRead)
        }
    }

    private suspend fun Server.serverHandleMessage(scope: CoroutineScope): Deferred<Message?> {
        return scope.async(Dispatchers.Default) {
            val serverSocket = aSocket(ActorSelectorManager(Dispatchers.IO)).tcp()
                .bind(InetSocketAddress(address, port))
            val clientSocket = serverSocket.accept()
            val input = clientSocket.openReadChannel()
            val output = clientSocket.openWriteChannel(autoFlush = true)
            val handler = MessageHandler(input, output)
            var msg: Message? = null

            delay(2000L)

            handler.receive {
                msg = it
            }

            msg?.let {
                handler.send(it)
            } ?: run {
                handler.send(byteArrayOf())
            }

            clientSocket.close()
            return@async msg
        }
    }

    private suspend fun Client.clientHandleMessage(scope: CoroutineScope): Deferred<Message?> {
        return scope.async(Dispatchers.Default) {
            val clientSocket = aSocket(ActorSelectorManager(Dispatchers.IO)).tcp()
                .connect(InetSocketAddress(address, port))
            val input = clientSocket.openReadChannel()
            val output = clientSocket.openWriteChannel(autoFlush = true)
            val handler = MessageHandler(input, output)

            handler.send(message)

            var response: Message? = null
            handler.receive {
                response = it
            }

            clientSocket.close()
            return@async response
        }
    }
}