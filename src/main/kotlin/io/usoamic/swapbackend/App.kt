package io.usoamic.swapbackend

import io.usoamic.swapbackend.other.Config
import java.nio.file.Files
import java.nio.file.Path

object App {
    @JvmStatic
    fun main(args: Array<String>) {
        val configName = if (args.isEmpty()) "config.json" else args[0]
        val json = Files.readString(Path.of(configName))
        val config = Config.fromJson(json)
        SwapBackend(config)
    }
}