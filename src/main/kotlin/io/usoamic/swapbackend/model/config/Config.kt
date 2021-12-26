package io.usoamic.swapbackend.model.config

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class Config (
    @SerializedName("db") val Db: Db,
    @SerializedName("aes") val Aes: Aes,
    @SerializedName("account") val Account: Account,
    @SerializedName("network") val Network: Network,
    @SerializedName("bot") val Bot: Bot?,
    @SerializedName("exchangeRate") val exchangeRate: BigDecimal,
    @SerializedName("ethThreshold") val ethThreshold: BigDecimal,
    @SerializedName("timeout") val Timeout: Long
) {
    val isTelegramEnabled get() = (Bot != null)

    companion object {
        fun fromJson(json: String): Config {
            return Gson().fromJson(json, Config::class.java)
        }
    }
}