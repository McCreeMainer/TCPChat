package common

object MessageType {
    const val DISCONNECT: Short = 1
    const val MESSAGE: Short = 2
    const val LOGIN_REQUIRED: Short = 3
    const val LOGIN_USER: Short = 4
    const val LOGIN_SUCCESSFUL: Short = 5
    const val ERROR: Short = 6
}

object ErrorCode {
    const val USER_LOGGED_IN = 0
    const val USER_ALREADY_EXIST = 1
    const val USER_BLANK = 2
}

fun Int.toByteArray() :ByteArray =
    ByteArray (Int.SIZE_BYTES) { i -> (this shr (i * 8)).toByte() }

fun ByteArray.toAsciiHexString() = joinToString("") {
    if (it in 32..127) it.toInt().toChar().toString() else
        "{${it.toUByte().toString(16).padStart(2, '0').uppercase()}}"
}
