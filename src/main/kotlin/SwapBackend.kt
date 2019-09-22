package io.usoamic.swapbackend

import io.usoamic.swapbackend.model.withdrawals
import io.usoamic.swapbackend.model.withdrawals.address
import io.usoamic.swapbackend.model.withdrawals.amount
import io.usoamic.swapbackend.model.withdrawals.id
import io.usoamic.swapbackend.model.withdrawals.status
import io.usoamic.swapbackend.other.Config
import io.usoamic.swapbackend.other.TxStatus
import io.usoamic.swapbackend.security.AesCipher
import io.usoamic.swapbackend.util.Log
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import io.usoamic.usoamickotlin.core.Usoamic
import io.usoamic.usoamickotlin.util.Coin
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

//https://github.com/JetBrains/Exposed
class SwapBackend(private val config: Config) {
    private val cipher: AesCipher = AesCipher(config.AES_METHOD, config.AES_KEY, config.AES_IV)
    private val usoamic = Usoamic(config.ACCOUNT_FILENAME, config.CONTRACT_ADDRESS, config.NODE)

    init {
        importPrivateKey()
        processNextTx()
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
                Log.d("exception: ${e.message}")
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
                            Log.d("Waiting confirmation...")
                            usoamic.waitTransactionReceipt(txHash) {
                                processNextTx()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }
}
