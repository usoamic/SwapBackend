package io.usoamic.swapbackend.util

class Log {
    enum class Type {
        DEBUG,
        RELEASE
    }
    companion object {
        var type = Type.DEBUG

        fun d(s: String) {
            if(type == Type.DEBUG) {
                println(s)
            }
        }
    }
}