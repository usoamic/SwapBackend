package io.usoamic.swapbackend

import io.usoamic.swapbackend.model.withdrawals
import io.usoamic.swapbackend.other.Config
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
//https://github.com/JetBrains/Exposed
class SwapBackend {
    init {
        Database.connect(
            url = Config.DB_URL,
            driver = Config.DB_DRIVER,
            user = Config.DB_USER,
            password = Config.DB_PASSWORD
        )

        transaction {
            SchemaUtils.create(withdrawals)

            withdrawals.selectAll().forEachIndexed { index, resultRow ->
                println("id: $index, $resultRow")
            //    resultRow[amount] = "a6" //change value
            }

            /*
            val stPeteId = Withdraws.insert {
                it[amount] = "1"
                it[address] = "2"
                it[time] = "3"
                it[status] = "4"
                it[email] = "5"
                it[type] = "6"
            } get Withdraws.id

            for (city in Withdraws.selectAll()) {
                println("${city[Withdraws.id]}: ${city[Withdraws.address]}")
            }

            println("size: $stPeteId")

             */
        }
    }
}