package io.usoamic.swapbackend.other

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import io.usoamic.swapbackend.model.Bot

data class Config (
    @SerializedName("db_url") val DB_URL: String,
    @SerializedName("db_driver") val DB_DRIVER: String,
    @SerializedName("db_user") val DB_USER: String,
    @SerializedName("db_password") val DB_PASSWORD: String,

    @SerializedName("aes_method") val AES_METHOD: String,
    @SerializedName("aes_key") val AES_KEY: String,
    @SerializedName("aes_iv") val AES_IV: String,

    @SerializedName("node") val NODE: String,
    @SerializedName("account_filename") val ACCOUNT_FILENAME: String,
    @SerializedName("account_password") val ACCOUNT_PASSWORD: String,
    @SerializedName("account_private_key") val ACCOUNT_PRIVATE_KEY: String,
    @SerializedName("contract_address") val CONTRACT_ADDRESS: String,

    @SerializedName("bot") val BOT: Bot?,

    @SerializedName("timeout") val TIMEOUT: Long
) {
    val isTelegramEnabled get() = (BOT != null)

    companion object {
        fun fromJson(json: String): Config {
            return Gson().fromJson(json, Config::class.java)
        }
    }
}