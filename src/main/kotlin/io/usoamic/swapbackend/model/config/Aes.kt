package io.usoamic.swapbackend.model.config

import com.google.gson.annotations.SerializedName

data class Aes (
    @SerializedName("method") val Method: String,
    @SerializedName("key") val Key: String,
    @SerializedName("iv") val IV: String
)