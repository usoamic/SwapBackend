package io.usoamic.swapbackend.model.config

import com.google.gson.annotations.SerializedName

data class Account (
    @SerializedName("filename") val Filename: String,
    @SerializedName("password") val Password: String,
    @SerializedName("private_key") val PrivateKey: String
)