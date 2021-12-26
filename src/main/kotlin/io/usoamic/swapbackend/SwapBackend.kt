package io.usoamic.swapbackend

import io.usoamic.swapbackend.model.config.Config
import io.usoamic.swapbackend.model.db.withdrawals
import io.usoamic.swapbackend.model.db.withdrawals.address
import io.usoamic.swapbackend.model.db.withdrawals.amount
import io.usoamic.swapbackend.model.db.withdrawals.id
import io.usoamic.swapbackend.model.db.withdrawals.status
import io.usoamic.swapbackend.other.TelegramBot
import io.usoamic.swapbackend.other.TxStatus
import io.usoamic.swapbackend.security.AesCipher
import io.usoamic.swapbackend.util.Log
import io.usoamic.usoamickt.core.Usoamic
import io.usoamic.usoamickt.enumcls.TxSpeed
import io.usoamic.usoamickt.util.Coin
import io.usoamic.usoamickt.util.DirectoryUtils
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.telegram.telegrambots.ApiContextInitializer
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.web3j.utils.Convert
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.system.exitProcess

//https://github.com/JetBrains/Exposed
class SwapBackend(private val config: Config) {
    private val cipher = AesCipher(
        method = config.Aes.Method,
        key = config.Aes.Key,
        iv = config.Aes.IV
    )
    private val usoamic = Usoamic(
        fileName = config.Account.Filename,
        filePath = DirectoryUtils.getDefaultKeyDirectory(),
        networkType = config.Network.Type,
        nodeProvider = config.Network.Node
    )
    private lateinit var ethBalance: BigDecimal
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

        if (::bot.isInitialized) {
            val tbApi = TelegramBotsApi()
            tbApi.registerBot(bot)
        }
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
            } catch (e: java.lang.Exception) {
                onException(e)
                onNoTransfers()
            }
        }
    }

    private fun onNoTransfers() {
        Log.d("- No transfers -")
        if (config.Timeout == -1L) {
            exitProcess(0)
        } else {
            Log.d("Waiting ${config.Timeout} secs...")
            Thread.sleep(config.Timeout * 1000)
            processNextTx()
        }
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
                            updateEthereumBalance()
                            Log.d("address: $address")

                            val txHash = usoamic.transferUso(
                                password = config.Account.Password,
                                to = address,
                                value = amount,
                                txSpeed = TxSpeed.Auto
                            )
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

    private fun updateEthereumBalance() {
        val weiBalance = usoamic.getEthBalance().toBigDecimal()
        ethBalance = Convert.fromWei(weiBalance, Convert.Unit.ETHER)
    }

    private fun sendNotification(message: String) {
        Log.d(message)
        if (::bot.isInitialized) {
            bot.sendNotification(message)
        }
    }

    private fun onException(e: Exception) {
        e.printStackTrace()
        val ethThreshold = config.ethThreshold

        val message = buildString {
            append("An error")
            append("(${e.javaClass})")
            append(" has occurred")
            if(e.message != null) {
                append(" with message ")
                append(e.message)
            }
            append(".")
            if(::ethBalance.isInitialized && ethBalance < ethThreshold) {
                append(" WARNING: balance ")
                append(ethBalance.setScale(3, RoundingMode.HALF_DOWN).toPlainString())
                append(" ETH.")
            }
        }

        sendNotification(message)
    }
}
