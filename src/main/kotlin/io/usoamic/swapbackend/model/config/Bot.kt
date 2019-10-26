package io.usoamic.swapbackend.model.config

import com.google.gson.annotations.SerializedName

data class Bot (
    @SerializedName("username") val Username: String,
    @SerializedName("token") val Token: String,
    @SerializedName("chatId") val ChatId: Long
)