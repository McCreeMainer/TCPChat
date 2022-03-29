package server

const val ADDRESS = "127.0.0.1"
const val PORT = 12345

const val HELP = "/help"
const val USERS = "/users"
const val BROADCAST = "/broadcast"
const val SHUTDOWN = "/shutdown"

const val HELP_MESSAGE = "$HELP\t\t- See available commands\n" +
        "$USERS\t\t- See connected users\n" +
        "$BROADCAST\t- Broadcast message\n" +
        "$SHUTDOWN\t- Shut down server"
