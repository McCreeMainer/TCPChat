package server

import kotlin.system.exitProcess

fun main(args: Array<String>) {
    try {
        val server = Server(ADDRESS, PORT, args.any { it == "-blocking" || it == "-b" })
        val cli = ServerCliManager(server)
        while (server.running) {
            cli.handleCommand(readLine())
        }
    } catch (e: Throwable) {
        e.printStackTrace()
        exitProcess(1)
    }
}

