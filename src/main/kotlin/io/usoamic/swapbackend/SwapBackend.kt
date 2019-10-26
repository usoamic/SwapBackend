package io.usoamic.swapbackend

import io.usoamic.swapbackend.model.db.withdrawals
import io.usoamic.swapbackend.model.db.withdrawals.address
import io.usoamic.swapbackend.model.db.withdrawals.amount
import io.usoamic.swapbackend.model.db.withdrawals.id
import io.usoamic.swapbackend.model.db.withdrawals.status
import io.usoamic.swapbackend.model.config.Config
import io.usoamic.swapbackend.other.TelegramBot
import io.usoamic.swapbackend.other.TxStatus
import io.usoamic.swapbackend.security.AesCipher
import io.usoamic.swapbackend.util.Log
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import io.usoamic.usoamickt.core.Usoamic
import io.usoamic.usoamickt.util.Coin
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.telegram.telegrambots.ApiContextInitializer
import org.telegram.telegrambots.meta.TelegramBotsApi

//https://github.com/JetBrains/Exposed
class SwapBackend(private val config: Config) {
    private val cipher = AesCipher(config.Aes.Method, config.Aes.Key, config.Aes.IV)
    private val usoamic = Usoamic(config.Account.Filename, config.Network.Type, config.Network.Node)
    private lateinit var bot: TelegramBot

    init {
        initTelegramBot()
        importPrivateKey()
        processNextTx()
    }

    private fun initTelegramBot() {
        ApiContextInitializer.init()
        config.Bot?.let {
            bot = TelegramBot(
                token = it.Token,
                chatId = it.ChatId,
                username = it.Username
            )
        }

        val tbApi = TelegramBotsApi()
        tbApi.registerBot(bot)
    }

    private fun importPrivateKey() {
        usoamic.importPrivateKey(config.Account.Password, config.Account.PrivateKey)
    }

    private fun connect() {
        Database.connect(
            url = config.Db.Url,
            driver = config.Db.Driver,
            user = config.Db.User,
            password = config.Db.Password
        )
    }

    private fun processNextTx() {
        cipher.encrypt(TxStatus.TX_PENDING.toId())?.let { encryptedStatus ->
            try {
                connect()
                transaction {
                    Log.d("==========")
                    val resultRow = withdrawals.select(status eq encryptedStatus).firstOrNull()
                    resultRow?.let { row ->
                        close()
                        processTx(row)
                    } ?: run {
                        close()
                        onNoTransfers()
                    }
                }
            }
            catch (e: ExposedSQLException) {
                onException(e)
                onNoTransfers()
            }
        }
    }

    private fun onNoTransfers() {
        Log.d("- No transfers -")
        Log.d("Waiting ${config.Timeout} secs...")
        Thread.sleep(config.Timeout*1000)
        processNextTx()
    }

    private fun processTx(resultRow: ResultRow) {
        val id = resultRow[id]
        cipher.decrypt(resultRow[status])?.toIntOrNull()?.let { status ->
            val txStatus = TxStatus.valueOf(status)
            Log.d("status: $txStatus")
            if (txStatus.isPending()) {
                cipher.decrypt(resultRow[amount])?.toBigIntegerOrNull()?.let { amount ->
                    cipher.decrypt(resultRow[address])?.let { address ->
                        try {
                            Log.d("address: $address")
                            val txHash = usoamic.transferUso(config.Account.Password, address, amount)
                            Log.d("New transfer: $txHash")
                            transaction {
                                withdrawals.update({ withdrawals.id eq id }) {
                                    it[withdrawals.status] = cipher.encrypt(TxStatus.TX_CONFIRMED.toId())!!
                                }
                                commit()
                            }
                            val numberOfCoins = Coin.fromSat(amount).toBigDecimal()

                            sendNotification("TxData: { $address, $numberOfCoins }")
                            Log.d("Waiting confirmation...")
                            usoamic.waitTransactionReceipt(txHash) {
                                sendNotification("TxHash: { $txHash }")
                                processNextTx()
                            }
                        } catch (e: Exception) {
                            onException(e)
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    private fun sendNotification(message: String) {
        println(message)
        if(::bot.isInitialized) {
            bot.sendNotification(message)
        }
    }

    private fun onException(e: Exception) {
        sendNotification("Exception(): ${e.javaClass}")
    }
}
