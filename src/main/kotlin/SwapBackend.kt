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
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

//https://github.com/JetBrains/Exposed
class SwapBackend {
    private val cipher: AesCipher = AesCipher(Config.AES_METHOD, Config.AES_KEY, Config.AES_IV)
    private val usoamic = Usoamic(Config.ACCOUNT_FILENAME, Config.CONTRACT_ADDRESS, Config.NODE)

    init {
        importPrivateKey()
        processTx()
    }

    private fun importPrivateKey() {
        usoamic.importPrivateKey(Config.ACCOUNT_PASSWORD, Config.ACCOUNT_PRIVATE_KEY)
    }

    private fun connect() {
        Database.connect(
            url = Config.DB_URL,
            driver = Config.DB_DRIVER,
            user = Config.DB_USER,
            password = Config.DB_PASSWORD
        )
    }

    private fun processTx() {
        connect()
        transaction {
            cipher.encrypt(TxStatus.TX_PENDING.toId())?.let { encryptedStatus ->
                val resultRow = withdrawals.select(status eq encryptedStatus).firstOrNull()
                resultRow?.let { row ->
                    close()
                    process(row) {
                        Thread.sleep(5000)
                        processTx()
                    }
                } ?: Log.d("No transfers")
            }
        }
    }

    private fun process(resultRow: ResultRow, callback: () -> Unit) {
        val id = resultRow[id]
        cipher.decrypt(resultRow[status])?.toIntOrNull()?.let { status ->
            val txStatus = TxStatus.valueOf(status)
            Log.d("status: $txStatus")
            if (txStatus.isPending()) {
                cipher.decrypt(resultRow[amount])?.toBigIntegerOrNull()?.let { amount ->
                    cipher.decrypt(resultRow[address])?.let { address ->
                        try {
                            Log.d("address: $address")
                            Log.d("amount: $amount")
                            val txHash = usoamic.transferUso(Config.ACCOUNT_PASSWORD, address, amount)
                            Log.d("New transfer: $txHash")
                            transaction {
                                withdrawals.update({ withdrawals.id eq id }) {
                                    it[withdrawals.status] = cipher.encrypt(TxStatus.TX_CONFIRMED.toId())!!
                                }
                                commit()
                            }
                            Log.d("Waiting confirmation...")
                            usoamic.waitTransactionReceipt(txHash) {
                                callback()
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
