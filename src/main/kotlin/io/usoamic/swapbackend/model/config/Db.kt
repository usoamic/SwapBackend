package io.usoamic.swapbackend.model.config

import com.google.gson.annotations.SerializedName

data class Db (
    @SerializedName("url") val Url: String,
    @SerializedName("driver") val Driver: String,
    @SerializedName("user") val User: String,
    @SerializedName("password") val Password: String
)