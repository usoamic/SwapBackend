package io.usoamic.swapbackend.other

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

class TelegramBot(
    private val token: String,
    private val chatId: Long,
    private val username: String
) : TelegramLongPollingBot() {
    fun sendNotification(message: String) {
        if(chatId == 0.toLong()) {
            println("Invalid chat id")
        }
        else {
            sendMessage(chatId, message)
        }
    }

    private fun sendMessage(chatId: Long, message: String) {
        try {
            execute(SendMessage(chatId, message))
        }
        catch (e: TelegramApiException) {
            e.printStackTrace()
        }
    }

    override fun getBotUsername(): String = username

    override fun getBotToken(): String = token

    override fun onUpdateReceived(update: Update?) {
        update?.message?.chatId?.let {
            sendMessage(it, "You chatId: $it")
        }
    }
}