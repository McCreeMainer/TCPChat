package client

class ClientCliManager(val client: Client) {

    fun handleCommand(input: String?) {
        if (!client.connected || input.isNullOrBlank()) return

        val parsedLine = input.trim().split("""\s+""".toRegex())

        when (parsedLine[0].lowercase()) {
            LOGIN -> {
                client.login(parsedLine[1])
            }
            SEND_FILE -> {
                client.sendFileMessage(parsedLine[1])
            }
            DISCONNECT -> {
                client.disconnect()
            }
            else -> {
                client.sendTextMessage(input)
            }
        }
    }
}