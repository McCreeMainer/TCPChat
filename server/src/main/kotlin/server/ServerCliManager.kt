package server

class ServerCliManager(val server: Server) {

    fun handleCommand(input: String?) {
        if (!server.running || input.isNullOrBlank()) return

        when (input.trim().lowercase().split("""\s+""".toRegex())[0]) {
            HELP -> {
                println(HELP_MESSAGE)
            }
            USERS -> {
                if (server.users.isEmpty()) {
                    println("No users")
                    return
                }

                println("Connected users:")
                server.users.map { Pair(it.username, it.socket.remoteAddress) }
                    .forEach {
                        it.first?.let { name -> println("$name ${it.second}") }
                    }
            }
            SHUTDOWN -> {
                server.stop()
            }
            else -> {
                server.broadcast(input)
            }
        }
    }
}