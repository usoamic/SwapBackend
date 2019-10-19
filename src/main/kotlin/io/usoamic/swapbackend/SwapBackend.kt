package io.usoamic.swapbackend

import io.usoamic.swapbackend.model.withdrawals
import io.usoamic.swapbackend.model.withdrawals.address
import io.usoamic.swapbackend.model.withdrawals.amount
import io.usoamic.swapbackend.model.withdrawals.id
import io.usoamic.swapbackend.model.withdrawals.status
import io.usoamic.swapbackend.other.Config
import io.usoamic.swapbackend.other.TelegramBot
import io.usoamic.swapbackend.other.TxStatus
import io.usoamic.swapbackend.security.AesCipher
import io.usoamic.swapbackend.util.Log
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import io.usoamic.usoamickotlin.core.Usoamic
import io.usoamic.usoamickotlin.util.Coin
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.telegram.telegrambots.ApiContextInitializer
import org.telegram.telegrambots.meta.TelegramBotsApi
import java.util.concurrent.TimeUnit
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService



//https://github.com/JetBrains/Exposed
class SwapBackend(private val config: Config) {
    private val cipher = AesCipher(config.AES_METHOD, config.AES_KEY, config.AES_IV)
    private val usoamic = Usoamic(config.ACCOUNT_FILENAME, config.CONTRACT_ADDRESS, config.NODE)
    private lateinit var bot: TelegramBot

    init {
        initTelegramBot()
        importPrivateKey()
        processNextTx()
    }

    private fun initTelegramBot() {
        ApiContextInitializer.init()
        config.BOT?.let {
            bot = TelegramBot(
                token = it.TOKEN,
                chatId = it.CHAT_ID,
                username = it.USERNAME
            )
        }

        val tbApi = TelegramBotsApi()
        tbApi.registerBot(bot)
    }

    private fun importPrivateKey() {
        usoamic.importPrivateKey(config.ACCOUNT_PASSWORD, config.ACCOUNT_PRIVATE_KEY)
    }

    private fun connect() {
        Database.connect(
            url = config.DB_URL,
            driver = config.DB_DRIVER,
            user = config.DB_USER,
            password = config.DB_PASSWORD
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
        Log.d("Waiting ${config.TIMEOUT} secs...")
        Thread.sleep(config.TIMEOUT*1000)
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
                            Log.d("amount: ${Coin.fromSat(amount).toBigDecimal()}")
                            val txHash = usoamic.transferUso(config.ACCOUNT_PASSWORD, address, amount)
                            Log.d("New transfer: $txHash")
                            transaction {
                                withdrawals.update({ withdrawals.id eq id }) {
                                    it[withdrawals.status] = cipher.encrypt(TxStatus.TX_CONFIRMED.toId())!!
                                }
                                commit()
                            }
                            bot.sendNotification("TxData: { $address, $amount }")
                            Log.d("Waiting confirmation...")
                            usoamic.waitTransactionReceipt(txHash) {
                                bot.sendNotification("TxHash: { $txHash }")
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

    private fun onException(e: Exception) {
        bot.sendNotification("Exception(): ${e.javaClass}")
    }
}
