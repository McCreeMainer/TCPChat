package client

import kotlinx.datetime.IllegalTimeZoneException
import kotlinx.datetime.TimeZone
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    try {
        val tzArgIndex = args.indexOf("-tz")
        var timeZone: TimeZone? = null
        if (tzArgIndex != -1 && tzArgIndex != args.lastIndex)
        try {
            timeZone = TimeZone.of(args[tzArgIndex + 1])
        } catch (e: IllegalTimeZoneException) {
            println("Incorrect Timezone")
        }
        val client = Client(ADDRESS, PORT, timeZone)
        val cli = ClientCliManager(client)
        while (client.connected) {
            cli.handleCommand(readLine())
        }
    } catch (e: Throwable) {
        e.printStackTrace()
        exitProcess(1)
    }
}
