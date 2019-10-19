package io.usoamic.swapbackend.model

import com.google.gson.annotations.SerializedName

data class Bot (
    @SerializedName("username") val USERNAME: String,
    @SerializedName("token") val TOKEN: String,
    @SerializedName("chatId") val CHAT_ID: Int
)