package io.usoamic.swapbackend

import io.usoamic.swapbackend.model.config.Config
import org.web3j.utils.Files
import java.io.File

object App {
    @JvmStatic
    fun main(args: Array<String>) {
        val configName = if (args.isEmpty()) "config.json" else args[0]
        val json = Files.readString(File(configName))
        val config = Config.fromJson(json)
        SwapBackend(config)
    }
}