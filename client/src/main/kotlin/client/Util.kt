package client

const val ADDRESS = "127.0.0.1"
const val PORT = 12345
const val FILE_REPO = "C:\\CNT\\repo"

const val HELP = "/help"
const val LOGIN = "/login"
const val SEND_FILE = "/file"
const val LOGOUT = "/logout"
const val DISCONNECT = "/disconnect"

const val HELP_MESSAGE = "$HELP\t\t- See available commands\n" +
        "$LOGIN\t\t- Login user [1]\n" +
        "$SEND_FILE\t\t- Send file by location [1]\n" +
        "$LOGOUT\t\t- Logout\n" +
        "$DISCONNECT\t- Disconnect"